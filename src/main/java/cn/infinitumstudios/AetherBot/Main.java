// 请注意，使用前千万要调整代码后使用，直接使用100%跑不起来!!!

package cn.infinitumstudios.AetherBot;

import cn.infinitumstudios.AetherBot.commands.CommandManager;
import cn.infinitumstudios.AetherBot.listeners.MessageListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Main {
    protected static MessageListener messageListener;
    protected static CommandManager commandManager;
    public static void main(String[] args) throws InterruptedException, IOException {
        Console console = System.console(); // TO-DO change to Scanner
        JSONObject config;

        File configFile = new File("./config.json");

        if (!configFile.exists()){

            configFile.createNewFile();
            config = defaultConfig();
            String formattedJsonString = formatJson(config.toString());

            Writer write = new OutputStreamWriter(Files.newOutputStream(configFile.toPath()), StandardCharsets.UTF_8);
            write.write(formattedJsonString);
            write.flush();
            write.close();

            System.out.println("请在config.json文件里修改配置后使用!");
            return;

        } else {
            config = new JSONObject(readfile(configFile));
        }

        messageListener = new MessageListener(config, Calendar.getInstance(Locale.CHINA).getTime().getTime());
        commandManager = new CommandManager(messageListener, config);

        messageListener.setIsFixingBot(false);

        JDA api = JDABuilder
                .createDefault(config.getJSONObject("discord").getString("token"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        api.addEventListener(messageListener, commandManager);
        api.awaitReady();

        try{
            for (Object s : config.getJSONObject("discord").getJSONArray("whitelistedChannelID")) {
                Objects.requireNonNull(
                                api.getTextChannelById(s.toString()))
                        .getGuild()
                        .modifyNickname(Objects.requireNonNull(Objects.requireNonNull(api.getTextChannelById(s.toString()))
                                        .getGuild()
                                        .getMemberById(config.getJSONObject("discord").getString("clientID"))),
                                "Aethers深井机器人").queue();
            }
        } catch (Exception e){
            System.err.println("[Error] 在修改机器人名称的时候出现问题");
            e.printStackTrace();
        }


        while (true){
            String input = console.readLine();
            if (input.startsWith("!")){
                if (input.startsWith("!setchannel ")){
                    input = input.replaceFirst("!setchannel ", "");
                    config.put("ListeningChannelID", input);
                    messageListener.setListeningChannel(input);
                } else if (input.equals("!reload")){
                    JSONObject reloadConfig = new JSONObject(readfile(new File("./config.json")));
                    commandManager.reloadConfig(reloadConfig);
                    messageListener.reloadConfig(reloadConfig);
                }
            } else {
                Objects.requireNonNull(api.getTextChannelById(config.getString("ListeningChannelID"))).sendMessage(input).queue();
            }
        }
    }
    public static JSONObject defaultConfig(){
        JSONObject config = new JSONObject();

        config.put("ListeningChannelID", "请在此输入你想默认监听的频道的ID，不会影响到指令");

        config.put("discord", new JSONObject());
        config.getJSONObject("discord").put("token", "请在此输入你的Discord APP (Bot) 的Token");
        config.getJSONObject("discord").put("clientID", "请在此输入你的Discord APP 的 Client ID");
        config.getJSONObject("discord").put("whitelistedChannelID", new JSONArray());
        config.getJSONObject("discord").getJSONArray("whitelistedChannelID").put("请在此输入你的机器人的白名单频道");
        config.getJSONObject("discord").getJSONArray("whitelistedChannelID").put("只有在这些频道里的人可以使用此Bot");
        config.getJSONObject("discord").getJSONArray("whitelistedChannelID").put("注意，这里是Array，不限制添加的数量");

        config.put("AISetting", new JSONObject());
        config.getJSONObject("AISetting").put("ChatGPTAPIKey", "这里填入你的OpenAI API的APIKey，这将和DALLE3共用");
        config.getJSONObject("AISetting").put("RolePlayGPTAPIKey", "这里填入你的角色扮演AI的APIKey");
        config.getJSONObject("AISetting").put("MidJourneyAPIKey", "这里填入你的Midjourney的APIKey");
        config.getJSONObject("AISetting").put("IdeogramAPIKey", "这里填入你的Ideogram的APIKey");
        config.getJSONObject("AISetting").put("SunoAPIKey", "这里填入你的Suno的APIKey");
        config.getJSONObject("AISetting").put("MaxHistory", "这里填机器人最多可以记多少条信息，请填入双数，");

        config.getJSONObject("AISetting").put("ChatGPT", new JSONObject());
        config.getJSONObject("AISetting").getJSONObject("ChatGPT").put("url", "填入你的ChatGPT API请求地址(/v1 结尾)");
        config.getJSONObject("AISetting").getJSONObject("ChatGPT").put("model", "chatgpt-4o-latest");
        config.getJSONObject("AISetting").getJSONObject("ChatGPT").put("prompt", "在此输入你想在每次对话时加上的Prompt");

        config.getJSONObject("AISetting").put("CatChat", new JSONObject());
        config.getJSONObject("AISetting").getJSONObject("CatChat").put("url", "填入你的角色扮演的AI API请求地址");
        config.getJSONObject("AISetting").getJSONObject("CatChat").put("model", "gpt-4");
        config.getJSONObject("AISetting").getJSONObject("CatChat").put("prompt1", "请在此输入你想要给角色扮演AI模型使用的Prompt");
        config.getJSONObject("AISetting").getJSONObject("CatChat").put("prompt2", "可以将Prompt分为很多段，但是如果你想要额外继续增加的话，请修改代码");

        config.getJSONObject("AISetting").put("PictureAI", new JSONObject());

        config.getJSONObject("AISetting").getJSONObject("PictureAI").put("MidJourney", new JSONObject());
        config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("MidJourney").put("url", "在此输入ModJourneyAPI请求地址");

        config.getJSONObject("AISetting").getJSONObject("PictureAI").put("DALLE3", new JSONObject());
        config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("DALLE3").put("url", "在此输入DALLE 3 API请求地址");

        config.getJSONObject("AISetting").getJSONObject("PictureAI").put("Ideogram", new JSONObject());
        config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").put("url", "在此输入Ideogram API请求地址");
        config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").put("model", "V_2_TURBO");

        config.put("suno", new JSONObject());
        config.getJSONObject("suno").put("url", "https://api.openai-hk.com/sunoapi");

        return config;
    }

    private static String readfile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        Reader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
        int ch = 0;
        StringBuilder sb = new StringBuilder();
        while ((ch = reader.read()) != -1) {
            sb.append((char) ch);
        }
        fileReader.close();
        reader.close();
        return sb.toString();
    }

    /**
     * 返回格式化JSON字符串。
     *
     * @param json 未格式化的JSON字符串。
     * @return 格式化的JSON字符串。
     */
    public static String formatJson(String json) {
        StringBuilder result = new StringBuilder();

        int length = json.length();
        int number = 0;
        char key = 0;

        // 遍历输入字符串。
        for (int i = 0; i < length; i++) {
            // 1、获取当前字符。
            key = json.charAt(i);

            // 2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                // （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }

                // （2）打印：当前字符。
                result.append(key);

                // （3）前方括号、前花括号，的后面必须换行。打印：换行。
                result.append('\n');

                // （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
                number++;
                result.append(indent(number));

                // （5）进行下一次循环。
                continue;
            }

            // 3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                // （1）后方括号、后花括号，的前面必须换行。打印：换行。
                result.append('\n');

                // （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                number--;
                result.append(indent(number));

                // （3）打印：当前字符。
                result.append(key);

                // （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }

                // （5）继续下一次循环。
                continue;
            }

            // 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            // 5、打印：当前字符。
            result.append(key);
        }

        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number; i++) {
            String SPACE = "   ";
            result.append(SPACE);
        }
        return result.toString();
    }
}