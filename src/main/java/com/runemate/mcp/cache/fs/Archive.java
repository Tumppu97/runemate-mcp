package com.runemate.mcp.cache.fs;

import com.runemate.mcp.cache.index.*;
import com.runemate.mcp.cache.io.*;
import com.runemate.mcp.cache.util.*;
import java.io.*;
import java.util.*;
import lombok.*;
import lombok.extern.log4j.*;
import org.jetbrains.annotations.*;

@Data
@Log4j2
@EqualsAndHashCode(of = { "archiveId", "name", "revision" })
public class Archive {

    @ToString.Exclude
    private final Index index;
    private final int archiveId;

    private int revision;
    private int crc;
    private int name;
    private int compressedSize;
    private int decompressedSize;
    private int compression;
    private FileData[] fileData;

    @Setter(AccessLevel.NONE)
    private Files files;

    private byte[] decompress(byte[] data) throws IOException {
        return decompress(data, null);
    }

    private byte[] decompress(byte[] data, int[] keys) throws IOException {
        if (data == null) {
            return null;
        }

        DataBlock block = DataBlock.decompress(data, keys);
        if (crc != block.crc()) {
            throw new IOException("CRC mismatch for " + index.getId() + "/" + archiveId);
        }

        if (block.revision() != -1 && block.revision() != revision) {
            log.warn("Revision mismatch in {}/{}: {} vs {}", index.getId(), archiveId, block.revision(), revision);
            setRevision(block.revision());
        }

        setCompression(block.compression());
        return block.data();
    }

    public Files files(JagexCache cache) throws IOException {
        if (files != null) {
            return files;
        }

        byte[] data = cache.load(this);
        return files(data, null);
    }

    public Files files(byte[] data, int[] keys) throws IOException {
        if (files != null) {
            return files;
        }

        return files = new Files(decompress(data, keys));
    }

    @Data
    public static class File {

        private final int fileId, name;
        byte[] contents;

        public Js5InputStream inputStream() {
            return new Js5InputStream(contents);
        }

        public int size() {
            return contents != null ? contents.length : 0;
        }

        public void decode(IncrementalDecoder decoder) throws IOException, UnhandledOpcodeException {
            try (Js5InputStream stream = inputStream()) {
                while (true) {
                    int opcode = stream.readUnsignedByte();
                    if (opcode == 0) {
                        break;
                    }
                    try {
                        decoder.accept(stream, opcode);
                    } catch (UnhandledOpcodeException e) {
                        log.warn("Unhandled opcode {} in file {} — returning partial definition", opcode, fileId);
                        break;
                    }
                }
            }
        }
    }

    public class Files implements Iterable<File> {

        private final LinkedHashMap<Integer, File> files = new LinkedHashMap<>();

        private Files(byte[] decompressed) throws IOException {
            for (FileData file : fileData) {
                File f = new File(file.getId(), file.getName());
                files.put(file.getId(), f);
            }

            if (files.size() == 1) {
                File f = files.values().iterator().next();
                f.setContents(decompressed);
                return;
            }

            int count = files.size();
            try (Js5InputStream in = new Js5InputStream(decompressed)) {
                in.setOffset(in.getLength() - 1);
                int chunks = in.readUnsignedByte();

                // Read structure
                in.setOffset(in.getLength() - 1 - chunks * count * 4);
                int[][] chunkSizes = new int[count][chunks];
                int[] fileSizes = new int[count];
                for (int chunk = 0; chunk < chunks; ++chunk) {
                    int chunkSize = 0;
                    for (int id = 0; id < count; ++id) {
                        int thisChunkSize = in.readInt();
                        chunkSize += thisChunkSize;
                        chunkSizes[id][chunk] = chunkSize;
                        fileSizes[id] += chunkSize;
                    }
                }

                byte[][] contents = new byte[count][];
                int[] offsets = new int[count];

                for (int i = 0; i < count; ++i) {
                    contents[i] = new byte[fileSizes[i]];
                }

                // Read file data
                in.setOffset(0);
                for (int chunk = 0; chunk < chunks; ++chunk) {
                    for (int id = 0; id < count; ++id) {
                        int chunkSize = chunkSizes[id][chunk];
                        in.readBytes(contents[id], offsets[id], chunkSize);
                        offsets[id] += chunkSize;
                    }
                }

                Iterator<File> itr = files.values().iterator();
                for (int id = 0; id < count; ++id) {
                    File f = itr.next();
                    f.setContents(contents[id]);
                }
            }
        }

        public Collection<File> files() {
            return Collections.unmodifiableCollection(files.values());
        }

        public int size() {
            return files.size();
        }

        public File get(int id) {
            return files.get(id);
        }

        @Override
        @NotNull
        public Iterator<File> iterator() {
            return files().iterator();
        }
    }
}
