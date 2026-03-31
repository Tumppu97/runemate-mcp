package com.runemate.mcp.cache.definition;

import lombok.*;
import java.util.*;

@Data
public class InventoryConfig {

    public int id;
    public int capacity;
    private Map<Integer, Object> params = null;
}
