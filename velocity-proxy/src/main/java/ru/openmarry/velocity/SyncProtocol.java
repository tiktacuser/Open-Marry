package ru.openmarry.velocity;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public final class SyncProtocol {

    private static final byte STATE = 1;
    private static final byte WEDDING = 2;

    private SyncProtocol() {
    }

    public static byte[] state(UUID player, UUID partner, String heartColorCode) {
        return write(output -> {
            output.writeByte(STATE);
            writeUuid(output, player);
            output.writeBoolean(partner != null);
            if (partner != null) {
                writeUuid(output, partner);
                output.writeUTF(heartColorCode);
            }
        });
    }

    public static byte[] wedding(UUID player, UUID partner) {
        return write(output -> {
            output.writeByte(WEDDING);
            writeUuid(output, player);
            writeUuid(output, partner);
        });
    }

    private static void writeUuid(DataOutputStream output, UUID uuid) throws IOException {
        output.writeLong(uuid.getMostSignificantBits());
        output.writeLong(uuid.getLeastSignificantBits());
    }

    private static byte[] write(MessageWriter writer) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            writer.write(output);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return bytes.toByteArray();
    }

    @FunctionalInterface
    private interface MessageWriter {
        void write(DataOutputStream output) throws IOException;
    }
}
