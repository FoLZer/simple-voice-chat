package de.maxhenkel.voicechat.api.NbtTags;

import net.minecraft.server.v1_16_R3.CrashReport;
import net.minecraft.server.v1_16_R3.CrashReportSystemDetails;
import net.minecraft.server.v1_16_R3.ReportedException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NbtIo {
    public static CompoundTag read(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
        Tag tag = readUnnamedTag(dataInput, 0, nbtAccounter);
        if (tag instanceof CompoundTag) {
            return (CompoundTag) tag;
        } else {
            throw new IOException("Root tag must be a named compound tag");
        }
    }

    public static void write(CompoundTag compoundTag, DataOutput dataOutput) throws IOException {
        writeUnnamedTag(compoundTag, dataOutput);
    }

    private static void writeUnnamedTag(Tag tag, DataOutput dataOutput) throws IOException {
        dataOutput.writeByte(tag.getId());
        if (tag.getId() != 0) {
            dataOutput.writeUTF("");
            tag.write(dataOutput);
        }
    }

    private static Tag readUnnamedTag(DataInput dataInput, int i, NbtAccounter nbtAccounter) throws IOException {
        byte b = dataInput.readByte();
        if (b == 0) {
            return EndTag.INSTANCE;
        } else {
            dataInput.readUTF();

            try {
                return TagTypes.getType(b).load(dataInput, i, nbtAccounter);
            } catch (IOException var7) {
                CrashReport crashReport = CrashReport.a(var7, "Loading NBT data");
                CrashReportSystemDetails crashReportCategory = crashReport.a("NBT Tag");
                crashReportCategory.a("Tag type", b);
                throw new ReportedException(crashReport);
            }
        }
    }
}
