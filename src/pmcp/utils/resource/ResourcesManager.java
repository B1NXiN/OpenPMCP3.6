package pmcp.utils.resource;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.IOUtils;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.utils.client.ClientCredentials;
import pmcp.utils.client.Helper;
import pmcp.utils.client.MinecraftInstance;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourcesManager extends MinecraftInstance {
    public File resources = new File(mc.gameDirectory, PMCP.Instance.getClientName() + "/resources");
    public File font = new File(resources, "font");
    public final Map<String, byte[]> res = new HashMap<>();

    public void init() {
        if (!resources.exists()) {
            if (!resources.mkdirs()) {
                System.err.println("Failed to create resource directory: " + resources.getAbsolutePath());
            } else {
                System.out.println("Resource directory created: " + resources.getAbsolutePath());
            }
        }
        if (!font.exists()) {
            if (!font.mkdirs()) {
                System.err.println("Failed to create font directory: " + font.getAbsolutePath());
            } else {
                System.out.println("Font directory created: " + font.getAbsolutePath());
            }
        }

        File fontFile = new File(font, "font.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/font.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }

        fontFile = new File(font, "icons.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/icons.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }

        fontFile = new File(font, "icon-noti.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/icon-noti.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }

        fontFile = new File(font, "profont.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/profont.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }

        fontFile = new File(font, "big.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/big.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }

        fontFile = new File(font, "classic.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/classic.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }
        fontFile = new File(font, "yuanshen.ttf");
        if (!fontFile.exists()) {
            try {
                fontFile.createNewFile();
                unpackFile(fontFile, "assets/minecraft/pmcp/font/yuanshen.ttf");
            } catch (IOException e) {
                Helper.sendSystemDeBug("字体无法生成：" + e, Helper.debug.B);
            }
        }
    }

    public ResourceLocation loadIconFromAbsolutePath(String absolutePath) {
        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + absolutePath);
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            NativeImage nativeImage = NativeImage.read(inputStream);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            return mc.getTextureManager().register("custom_icon_" + file.getName(), dynamicTexture);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResourceLocation loadFile(String absolutePath) {
        File file = new File(absolutePath);

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + absolutePath);
        }
        return null;
    }

    public byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream input = inStream;
             ByteArrayOutputStream output = outStream) {
            while ((len = input.read(buffer)) != -1)
                output.write(buffer, 0, len);
            return output.toByteArray();
        }
    }

    public InputStream getStream(String name) {
        if (res.containsKey(name))
            return new ByteArrayInputStream(res.get(name));
        File file = new File(resources, name);
        try {
            if (file.exists())
                return Files.newInputStream(file.toPath());
        } catch (Throwable ignored) {}
        return ResourcesManager.class.getResourceAsStream("/" + name);
    }

    public byte[] get(String name) {
        InputStream stream = getStream(name);
        if (stream != null) {
            try {
                return readStream(stream);
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public void unpackFile(File file, String name) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file);
        try {
            IOUtils.copy(Objects.requireNonNull(PMCP.class.getClassLoader().getResourceAsStream(name)), fos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
