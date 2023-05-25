package com.voltskiya.structure.database;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.bukkit.NamespacedKey;

public class NamespaceSerializer extends StdSerializer<NamespacedKey> {


    protected NamespaceSerializer() {
        super(NamespacedKey.class);
    }

    @Override
    public void serialize(NamespacedKey value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.toString());
    }
}
