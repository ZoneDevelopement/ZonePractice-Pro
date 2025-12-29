package dev.nandi0813.practice.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public enum ItemSerializationUtil {
    ;

    @Nullable
    public static String itemStackArrayToBase64(ItemStack[] items) {
        if (items == null)
            return null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error encoding base 64 itemstack.");
        }
        return null;
    }

    public static ItemStack[] itemStackArrayFromBase64(String data) {
        if (data == null) {
            return new ItemStack[0];
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                ItemStack itemStack = (ItemStack) dataInput.readObject();
                items[i] = itemStack;
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error decoding base 64 itemstack.");
        }
        return null;
    }

}