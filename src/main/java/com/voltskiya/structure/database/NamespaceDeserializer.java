package com.voltskiya.structure.database;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import org.bukkit.NamespacedKey;

public class NamespaceDeserializer extends StdDeserializer<NamespacedKey> {

    protected NamespaceDeserializer() {
        super(NamespacedKey.class);
    }

    @Override
    public NamespacedKey deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String[] value = p.readValueAs(String.class).split(":");
        return new NamespacedKey(value[0], value[1]);
    }

}
