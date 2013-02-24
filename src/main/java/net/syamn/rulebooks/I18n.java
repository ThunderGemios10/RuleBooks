/**
 * RuleBooks - Package: net.syamn.rulebooks Created: 2013/02/24 9:11:24
 */
package net.syamn.rulebooks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.syamn.utils.LogUtil;
import net.syamn.utils.StrUtil;
import net.syamn.utils.Util;
import net.syamn.utils.file.FileStructure;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * I18n (I18n.java)
 * 
 * @author syam(syamn)
 */
public class I18n {
    private static final String languageDirName = "languages";

    private static Configuration fallbackMessages;
    private static Configuration messages;

    /**
     * Init I18n
     * 
     * @param locale
     *            set current locale
     */
    public static void init(final String locale) {
        // extract languages
        extractLanguageFiles(false);

        // load default (fallback) language: English
        try {
            fallbackMessages = loadLanguageFile("default");
        } catch (Exception ex) {
            LogUtil.warning("Could not load default(default.yml) messages file!");
        }

        // load custom language
        try {
            setCurrentLanguage(locale);
        } catch (Exception ex) {
            LogUtil.warning("Could not load messages for " + locale + ": using default.yml");
            messages = fallbackMessages;
        }
    }

    // Extract methods
    public static void extractLanguageFiles(final boolean force) {
        final File langDir = getLanguagesDir();
        FileStructure.createDir(langDir);

        // extract resources
        List<String> locales = new ArrayList<String>();

        locales.add("default");
        locales.add("ja-jp");

        for (String locale : locales) {
            FileStructure.extractResource("/languages/" + locale + ".yml", langDir, force, true, RuleBooks.getInstance());
        }
    }

    // Load methods
    public static void setCurrentLanguage(final String locale) throws Exception {
        messages = loadLanguageFile(locale);
    }

    private static Configuration loadLanguageFile(final String locale) throws Exception {
        final File langDir = getLanguagesDir();
        File file = new File(langDir, locale + ".yml");

        // check file available
        if (file == null || !file.isFile() || !file.canRead()) {
            LogUtil.warning("Unknown language file: " + locale);
            return null;
        }

        YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

        // check all messages available
        if (fallbackMessages != null && conf.getKeys(true).size() != fallbackMessages.getKeys(true).size()) {
            // collect missing message keys
            for (String key : fallbackMessages.getKeys(true)) {
                if (!conf.contains(key) && !fallbackMessages.isConfigurationSection(key)) {
                    conf.set(key, fallbackMessages.get(key));
                    LogUtil.warning("Missing message key on " + locale + ".yml: " + key);
                }
            }
        }
        return conf;
    }

    /* ***** Begin replace words ***** */
    public static final String BOOK_NAME = "%BOOK_NAME%";
    public static final String COST = "%COST%";
    public static final String COUNT = "%COUNT%";

    /* ***** End replace words ******* */

    public static String _(final String key, final Object... args) {
        // message file not proper loaded
        if (messages == null) {
            LogUtil.warning("Localized messages file is NOT loaded..");
            return "!" + key + "!";
        }

        String msg = getString(messages, key);

        // missing key
        if (msg == null || msg.length() == 0) {
            if (msg == null)
                LogUtil.warning("Missing message key '" + key + "'");
            msg = getString(fallbackMessages, key);
            if (msg == null || msg.length() == 0) {
                LogUtil.warning("Delete language files and type '/rulebooks reload' to regenerate them.");
                return "!" + key + "!";
            }
        }

        // coloring
        msg = Util.coloring(msg);

        // build replaces
        Map<String, Object> binds = buildBinds(args);
        for (String bindKey : binds.keySet()) {
            if (bindKey == null)
                continue;
            final Object obj = binds.get(bindKey);
            msg = msg.replace(bindKey, ((obj != null) ? obj.toString() : ""));
        }

        return msg;
    }

    private static Map<String, Object> buildBinds(final Object... args) {
        Map<String, Object> bind = new HashMap<String, Object>(args.length / 2);

        if (args == null || args.length < 2) { return bind; }

        for (int i = 0; i < args.length; i += 2) {
            if ((i + 2) > args.length) {
                break;
            }

            bind.put(args[i].toString(), args[i + 1]);
        }

        return bind;
    }

    private static String getString(final Configuration conf, final String key) {
        String s = null;
        Object o = conf.get(key);

        if (o instanceof String) {
            s = o.toString();
        } else if (o instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> l = (List<String>) o;
            s = StrUtil.join(l, "\n");
        }

        return s;
    }

    /**
     * Get languages directory
     * 
     * @return File
     */
    private static File getLanguagesDir() {
        return new File(RuleBooks.getInstance().getDataFolder(), languageDirName);
    }
}
