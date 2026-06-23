/*
 * Decompiled with CFR 0.152.
 */
package me.chayapak1.chomens_bot.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import me.chayapak1.chomens_bot.Main;
import me.chayapak1.chomens_bot.command.TrustLevel;
import me.chayapak1.chomens_bot.data.keys.Key;
import me.chayapak1.chomens_bot.data.keys.KeysData;
import me.chayapak1.chomens_bot.data.player.PlayerEntry;
import me.chayapak1.chomens_bot.util.LoggerUtilities;
import me.chayapak1.chomens_bot.util.RandomStringUtilities;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class HashingUtilities {
    public static final KeyManager KEY_MANAGER = new KeyManager();
    public static final Map<Long, Pair<TrustLevel, String>> discordHashes = new ConcurrentHashMap<Long, Pair<TrustLevel, String>>();

    public static void init() {
    }

    public static String getHash(String key, String prefix, PlayerEntry sender) {
        long time = System.currentTimeMillis() / 5000L;
        String hashInput = sender.profile.getIdAsString() + prefix + time + key;
        return Hashing.sha256().hashString(hashInput, StandardCharsets.UTF_8).toString().substring(0, 16);
    }

    private static String getFixedHashInput(String input) {
        String sanitizedInput = input;
        if (input.length() == 34 && input.endsWith("\u00a7r")) {
            sanitizedInput = input.substring(0, input.length() - 2);
        }
        sanitizedInput = sanitizedInput.replace("\u00a7", "");
        return sanitizedInput;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static TrustLevel getPlayerHashTrustLevel(String input, String prefix, PlayerEntry sender) {
        List<KeysData> keys2;
        String fixedInput = HashingUtilities.getFixedHashInput(input);
        List<KeysData> list = keys2 = HashingUtilities.KEY_MANAGER.keys;
        synchronized (list) {
            for (KeysData keysData : keys2) {
                for (Key keyObject : keysData.keys()) {
                    String hashed = HashingUtilities.getHash(keyObject.key(), prefix, sender);
                    if (!fixedInput.equals(hashed)) continue;
                    return keyObject.trustLevel();
                }
            }
        }
        return null;
    }

    public static boolean isCorrectDiscordHash(String input) {
        String fixedInput = HashingUtilities.getFixedHashInput(input);
        for (Pair<TrustLevel, String> pair : discordHashes.values()) {
            if (!pair.getValue().equals(fixedInput)) continue;
            return true;
        }
        return false;
    }

    public static TrustLevel getDiscordHashTrustLevel(String input) {
        for (Map.Entry<Long, Pair<TrustLevel, String>> entry : new ArrayList<Map.Entry<Long, Pair<TrustLevel, String>>>(discordHashes.entrySet())) {
            Pair<TrustLevel, String> pair = entry.getValue();
            if (!pair.getRight().equals(input)) continue;
            discordHashes.remove(entry.getKey());
            return pair.getLeft();
        }
        return TrustLevel.PUBLIC;
    }

    public static TrustLevel getTrustLevel(String input, String prefix, PlayerEntry sender) {
        TrustLevel playerHashTrustLevel = HashingUtilities.getPlayerHashTrustLevel(input, prefix, sender);
        if (playerHashTrustLevel != null) {
            return playerHashTrustLevel;
        }
        if (HashingUtilities.isCorrectDiscordHash(input)) {
            return HashingUtilities.getDiscordHashTrustLevel(input);
        }
        return TrustLevel.PUBLIC;
    }

    public static String generateDiscordHash(long userId, TrustLevel trustLevel) {
        String string = RandomStringUtilities.generate(16, RandomStringUtilities.ALPHANUMERIC);
        discordHashes.putIfAbsent(userId, Pair.of(trustLevel, string));
        return discordHashes.get(userId).getRight();
    }

    public static class KeyManager {
        private static final Path KEY_PATH = Path.of("keys.json", new String[0]);
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        public List<KeysData> keys = null;

        public KeyManager() {
            try {
                this.initialLoad();
            }
            catch (IOException e) {
                LoggerUtilities.error("Failed to load the keys!");
                LoggerUtilities.error(e);
                return;
            }
            Main.EXECUTOR.scheduleAtFixedRate(this::write, 1L, 1L, TimeUnit.MINUTES);
            Runtime.getRuntime().addShutdownHook(new Thread(this::write));
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Nullable
        public String generate(TrustLevel level, String userId, boolean force, String alreadyExistsMessage) throws IllegalStateException {
            if (this.keys == null) {
                return null;
            }
            List<KeysData> keys2 = this.keys;
            KeysData data = null;
            List<KeysData> list = keys2;
            synchronized (list) {
                for (KeysData keysData : keys2) {
                    if (!keysData.userId().equals(userId)) continue;
                    data = keysData;
                    break;
                }
            }
            String generatedKey = RandomStringUtilities.generate(48, RandomStringUtilities.ALPHANUMERIC);
            if (data == null) {
                data = new KeysData(new ArrayList<Key>(), userId);
                data.keys().add(new Key(level, generatedKey, System.currentTimeMillis()));
                keys2.add(data);
            } else {
                for (Key key : new ArrayList<Key>(data.keys())) {
                    if (!key.trustLevel().equals((Object)level)) continue;
                    if (!force) {
                        throw new IllegalStateException(alreadyExistsMessage);
                    }
                    data.keys().remove(key);
                }
                data.keys().add(new Key(level, generatedKey, System.currentTimeMillis()));
            }
            this.write();
            return generatedKey;
        }

        private void initialLoad() throws IOException {
            if (Files.exists(KEY_PATH, new LinkOption[0])) {
                try (BufferedReader reader = Files.newBufferedReader(KEY_PATH);){
                    this.keys = Collections.synchronizedList((List)OBJECT_MAPPER.readValue((Reader)reader, (JavaType)OBJECT_MAPPER.getTypeFactory().constructCollectionType(ObjectArrayList.class, KeysData.class)));
                }
            } else {
                Files.createFile(KEY_PATH, new FileAttribute[0]);
                this.keys = Collections.synchronizedList(new ObjectArrayList());
            }
        }

        private void write() {
            try (BufferedWriter writer = Files.newBufferedWriter(KEY_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);){
                writer.write(OBJECT_MAPPER.writeValueAsString(this.keys));
            }
            catch (IOException e) {
                LoggerUtilities.error("Failed to write the keys file!");
                LoggerUtilities.error(e);
            }
        }
    }
}

