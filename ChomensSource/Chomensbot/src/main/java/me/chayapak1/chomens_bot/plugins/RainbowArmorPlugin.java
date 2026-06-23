/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.plugins;

import java.awt.Color;
import java.util.HashMap;
import me.chayapak1.chomens_bot.Bot;
import me.chayapak1.chomens_bot.data.listener.Listener;
import me.chayapak1.chomens_bot.selfCares.essentials.VanishSelfCare;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.IntComponentType;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.ServerboundSetCreativeModeSlotPacket;

public class RainbowArmorPlugin
implements Listener {
    private static final int[] LEATHER_ARMORS = new int[]{913, 914, 915, 916};
    private final Bot bot;
    private final VanishSelfCare vanish;
    private float rainbowHue = 0.0f;

    public RainbowArmorPlugin(Bot bot) {
        this.bot = bot;
        this.vanish = bot.selfCare.find(VanishSelfCare.class);
        if (!bot.config.rainbowArmor) {
            return;
        }
        bot.listener.addListener(this);
    }

    @Override
    public void onTick() {
        if (!this.bot.config.rainbowArmor || !this.vanish.visible) {
            return;
        }
        int increment = 18;
        Color color = Color.getHSBColor(this.rainbowHue / 360.0f, 1.0f, 1.0f);
        int rgbColor = color.getRGB() & 0xFFFFFF;
        this.rainbowHue = (this.rainbowHue + 18.0f) % 360.0f;
        HashMap map = new HashMap();
        IntComponentType type = DataComponentTypes.DYED_COLOR;
        IntComponentType.IntDataComponentFactory factory2 = (IntComponentType.IntDataComponentFactory)type.getDataComponentFactory();
        map.put(type, factory2.createPrimitive(type, rgbColor));
        DataComponents dataComponents = new DataComponents(map);
        for (int i = 0; i < 4; ++i) {
            this.bot.session.send(new ServerboundSetCreativeModeSlotPacket((short)(i + 5), new ItemStack(LEATHER_ARMORS[i], 1, dataComponents)));
        }
    }
}

