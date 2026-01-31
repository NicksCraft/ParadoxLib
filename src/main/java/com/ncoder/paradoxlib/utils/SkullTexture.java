package com.ncoder.paradoxlib.utils;

import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.CommonPatterns;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SkullTexture {

    private final String url;
    private final String base64;
    private final String texture;
    private final String hexadecimal;

    public SkullTexture(@Nonnull String texture) {
        Validate.notNull(texture, "Texture cannot be null");

        if (texture.startsWith("ey")) {
            this.base64 = texture;

            this.texture = new String(Base64.getDecoder().decode(texture));

            this.url = this.texture.substring(this.texture.indexOf("\"url\"") + 7, this.texture.lastIndexOf("\"}}}"));

            if (this.url.contains("http://textures.minecraft.net/texture/"))
                this.hexadecimal = this.url.substring(38);
            else
                this.hexadecimal = null;
        } else if (texture.startsWith("https://") || texture.startsWith("http://")) {
            this.url = texture;

            this.texture = "{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}";

            if (texture.contains("http://textures.minecraft.net/texture/"))
                this.hexadecimal = texture.substring(38);
            else
                this.hexadecimal = null;

            this.base64 = Base64.getEncoder().encodeToString(this.texture.getBytes(StandardCharsets.UTF_8));
        } else if (texture.startsWith("{\"textures\"")) {
            this.url = texture.substring(texture.indexOf("\"url\"") + 7, texture.lastIndexOf("\"}}}"));

            this.texture = texture;

            if (this.url.contains("http://textures.minecraft.net/texture/"))
                this.hexadecimal = this.url.substring(38);
            else
                this.hexadecimal = null;

            this.base64 = Base64.getEncoder().encodeToString(texture.getBytes(StandardCharsets.UTF_8));
        } else if (CommonPatterns.HEXADECIMAL.matcher(texture).matches()) {
            this.hexadecimal = texture;

            this.url = "http://textures.minecraft.net/texture/" + texture;

            this.texture = "{\"textures\":{\"SKIN\":{\"url\":\"" + this.url + "\"}}}";

            this.base64 = Base64.getEncoder().encodeToString(this.texture.getBytes(StandardCharsets.UTF_8));
        } else {
            this.url = null;
            this.base64 = null;
            this.texture = null;
            this.hexadecimal = null;
            throw new IllegalArgumentException("The provided texture does not seem to be a valid texture!");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getBase64() {
        return base64;
    }

    public String getTexture() {
        return texture;
    }

    public String getHexadecimal() {
        return hexadecimal;
    }

    public static PlayerSkin getAsSkin(final SkullTexture texture) {
        return PlayerSkin.fromBase64(texture.getBase64());
    }

    public static ItemStack getAsItemStack(final SkullTexture texture) {
        return SlimefunUtils.getCustomHead(texture.getBase64());
    }

}
