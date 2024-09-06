package cn.infinitumstudios.AetherBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageListener extends ListenerAdapter
{
    MessageChannel channel;
    String ChatGPTAPIKey, MidjourneyAPIKey, IdeogramAPIKey, SunoAPIKey, RolePlayAPIKey;
    String model;
    String chatUrl;
    String pictureUrl;
    String listeningChannelID;
    String mjUrl;
    String prompt;
    String clientID;
    String catModel;
    String catPrompt, catPrompt2;
    String catUrl;
    String ideogramUrl;
    String ideogramModel;
    String sunoUrl;
    int maxHistory;
    List<String> whitelistedServer;
    JSONObject database;
    String patterStr = "yyyy-MM-dd HH:mm:ss";
    protected boolean isFixingBot = false;
    long serverStartTime;
    boolean catPromptEnable;
    public MessageListener(JSONObject config, long time){
        database = new JSONObject();

        database.put("mj", new JSONObject());
        database.put("mjByMissionID", new JSONObject());
        database.put("ChatHistory", new JSONObject());
        database.put("CatChatHistory", new JSONObject());

        database.put("config", config);
        database.put("suno", new JSONObject());
        database.getJSONObject("suno").put("request", new JSONObject());

        ChatGPTAPIKey = config.getJSONObject("AISetting").getString("ChatGPTAPIKey");
        MidjourneyAPIKey = config.getJSONObject("AISetting").getString("MidJourneyAPIKey");
        IdeogramAPIKey = config.getJSONObject("AISetting").getString("IdeogramAPIKey");
        SunoAPIKey = config.getJSONObject("AISetting").getString("SunoAPIKey");
        RolePlayAPIKey = config.getJSONObject("AISetting").getString("RolePlayGPTAPIKey");

        model = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("model");
        chatUrl = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("url");
        pictureUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("DALLE3").getString("url");
        listeningChannelID = config.getString("ListeningChannelID");
        mjUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("MidJourney").getString("url");
        prompt = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("prompt");
        clientID = config.getJSONObject("discord").getString("clientID");
        catModel = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("model");
        catPrompt = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("prompt1");
        catPrompt2 = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("prompt2");
        catUrl = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("url");
        maxHistory = config.getJSONObject("AISetting").getInt("MaxHistory");
        ideogramModel = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").getString("model");
        ideogramUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").getString("url");
        sunoUrl = config.getJSONObject("suno").getString("url");

        whitelistedServer = new ArrayList<>();
        JSONArray wja = config.getJSONObject("discord").getJSONArray("whitelistedChannelID");
        for (int i = 0;i < wja.length();i++){
            whitelistedServer.add(wja.getString(i));
        }

        serverStartTime = time;
        catPromptEnable = false;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        channel = event.getChannel();

        if (event.getChannel().getId().equals(listeningChannelID)){
            System.out.println(event.getAuthor().getEffectiveName() + ": " + event.getMessage().getContentRaw());
        }

        if (!whitelistedServer.contains(channel.getId())){
            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (event.getAuthor().getName().equals("Aethers深井机器人")) return;

        if (event.getAuthor().isBot() && !content.equals("我上早八")) return;

        if (content.equals("!id")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            channel.sendMessage(channel.getId()).queue();
        }

        if (content.equals("!cat")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (catPromptEnable){
                catPromptEnable = false;
                channel.sendMessage("猫娘模式关闭！").queue();
                event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(Long.parseLong(clientID))), "Aethers深井机器人").queue();
                return;
            }

            catPromptEnable = true;
            channel.sendMessage("猫娘模式启动！输入 !c <任何互动> 即可与我互动喵～").queue();
            event.getGuild().modifyNickname(Objects.requireNonNull(event.getGuild().getMemberById(Long.parseLong(clientID))), "Aethers猫娘机器人").queue();
            return;
        }

        if (content.equals("!ping")) {
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            if (catPromptEnable){
                channel.sendMessage("Pong！Aether猫粮机器人在线！").queue();
            } else {
                channel.sendMessage("Pong！Aether深井机器人在线！").queue();
            }
        }

        if (content.equals("TD")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("已退订该消息").queue();
        }

        if (content.equals("R")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("已拒收该消息").queue();
        }

        if (content.equals("我上早八")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("666").queue();
        }

        if (content.equals("6")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("9").queue();
        }

        if (content.equals("666")){
            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("999").queue();
        }

        if (content.startsWith("!chat ") || content.startsWith("!c ")) {

            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (content.startsWith("!chat ")) content = content.replaceFirst("!chat ", "");
            if (content.startsWith("!c ")) content = content.replaceFirst("!c ", "");

            // HTTP request
            String reply;
            if (catPromptEnable){
                if (!database.getJSONObject("CatChatHistory").has(message.getAuthor().getId())){
                    database.getJSONObject("CatChatHistory").put(message.getAuthor().getId(), new JSONArray());
                }
                reply = POSTRequest(chatUrl, getOpenAIChatRequestBody(content, catPrompt, database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId())).toString(),ChatGPTAPIKey,channel);
            } else {
                if (!database.getJSONObject("ChatHistory").has(message.getAuthor().getId())){
                    database.getJSONObject("ChatHistory").put(message.getAuthor().getId(), new JSONArray());
                }
                reply = POSTRequest(chatUrl, getOpenAIChatRequestBody(content, prompt, database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId())).toString(),RolePlayAPIKey,channel);
            }

            if (reply == null) {
                channel.sendMessage("出现内部错误，指令执行失败").queue();
                System.err.println("API请求数据返回无效值");
                return;
            }

            try {
                // 此处请根据API文档进行修改！
                JSONObject replyJSON = new JSONObject(reply);
                String output = replyJSON
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                if (output == null){
                    channel.sendMessage("出现内部错误，指令执行失败").queue();
                    return;
                }

                if (output.startsWith("抱歉") && catPromptEnable){
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> 对不起，我不能满足这个请求，喵。").queue();
                    return;
                }

                if (output.contains("<|endoftext|>")){
                    output = output.replace("<|endoftext|>", "");
                }

                // 历史聊天记录
                if (catPromptEnable){
                    if (database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).length() == maxHistory){
                        Random rand = new Random();
                        int randNumber = rand.nextInt(26) + 5;
                        database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).remove(randNumber * 2);
                        database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).remove((randNumber * 2) + 1);
                    }
                    int index = database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).length();
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "user");
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", content);

                    index ++;
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "assistant");
                    database.getJSONObject("CatChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", output);
                } else {

                    if (database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).length() == maxHistory){
                        Random rand = new Random();
                        int randNumber = rand.nextInt(26) + 5;
                        database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).remove(randNumber * 2);
                        database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).remove((randNumber * 2) + 1);
                    }

                    int index = database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).length();
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "user");
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", content);

                    index ++;
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "assistant");
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", output);
                }

                if (output.length() >= 2000){
                    longMessage(output, channel, message.getAuthor().getId());
                } else {
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> " + output).queue();
                }

            } catch (Exception e) {
                channel.sendMessage("出现内部错误，指令执行失败").queue();
                try {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (content.contains("Aethers深井机器人") || content.contains("<@"+ clientID +">")){

            // 请把此处修改为你的名字
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (!database.getJSONObject("ChatHistory").has(message.getAuthor().getId())){
                database.getJSONObject("ChatHistory").put(message.getAuthor().getId(), new JSONArray());
            }

            if(content.contains("<@"+ clientID +">"))content = content.replaceAll("<@"+ clientID +">", "@Aethers深井机器人 ");

            // Post
            String reply = null;

            if (!catPromptEnable) reply = POSTRequest(chatUrl, getOpenAIChatRequestBody(content, prompt, database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId())).toString(),ChatGPTAPIKey,channel);
            else {
                channel.sendMessage("猫娘模式只能通过 !c / !chat 开启对话，无法使用@").queue();
                return;
            }

            if (reply == null) return;

            // to json object
            try {
                JSONObject replyJSON = new JSONObject(reply);
                String output = replyJSON
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                if (output == null){
                    channel.sendMessage("出现内部错误，指令执行失败").queue();
                    return;
                }

                if (database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).length() == maxHistory){
                    Random rand = new Random();
                    int randNumber = rand.nextInt(26) + 5;
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).remove(randNumber * 2);
                    database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).remove((randNumber * 2) + 1);
                }
                int index = database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).length();
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "user");
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", content);

                index ++;
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).put(index, new JSONObject());
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("role", "assistant");
                database.getJSONObject("ChatHistory").getJSONArray(message.getAuthor().getId()).getJSONObject(index).put("content", output);

                // result
                if (output.length() >= 2000){
                    longMessage(output, channel, message.getAuthor().getId());
                } else {
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> " + output).queue();
                }

            } catch (Exception e) {
                channel.sendMessage("出现内部错误，指令执行失败").queue();
                try {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        // picture generation
        if (content.startsWith("!dalle3 ") || content.startsWith("!d3 ")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (content.startsWith("!dalle3 ")) content = content.replaceFirst("!dalle3 ", "");
            if (content.startsWith("!d3 ")) content = content.replaceFirst("!d3 ", "");

            if (content.length() <= 5){
                channel.sendMessage("请求失败！提示词长度过短！").queue();
                return;
            }

            channel.sendMessage("收到请求，正在使用DALL·E 3生成图片中...").queue();
            try {
                String respond = POSTRequest(pictureUrl, getOpenAIImageRequestBody(content).toString(),ChatGPTAPIKey, channel);
                if (respond == null){
                    channel.sendMessage("出现内部错误，指令执行失败(Request respond is null)").queue();
                    return;
                }
                JSONObject respondJSON = new JSONObject(respond);
                String pictureURL = respondJSON
                        .getJSONArray("data")
                        .getJSONObject(0)
                        .getString("url");
                channel.sendMessage("<@"+message.getAuthor().getId()+"> [这是生成的图片: ](" + pictureURL + ")").queue();
            } catch (Exception e){
                channel.sendMessage("出现内部错误，指令执行失败(Respond -> JSON)").queue();
                throw e;
            }
        }

        if (content.startsWith("!midjourney ") || content.startsWith("!mj ")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (content.startsWith("!midjourney ")) content = content.replaceFirst("!midjourney ", "");
            if (content.startsWith("!mj ")) content = content.replaceFirst("!mj ", "");

            if (content.length() <= 5){
                channel.sendMessage("请求失败！提示词长度过短！").queue();
                return;
            }

            channel.sendMessage("收到请求，正在使用MidJourney生成图片中...").queue();
            String postReply = POSTRequest(mjUrl + "/mj/submit/imagine", getModJourneyRequestBody(content).toString(), MidjourneyAPIKey, channel);
            if (postReply == null){
                channel.sendMessage("出现内部错误，指令执行失败(Post)").queue();
                return;
            }

            JSONObject postResponse = new JSONObject(postReply);

            channel.sendMessage("Midjourney任务ID: " + postResponse.getString("result")).queue();
            database.getJSONObject("mj").put(message.getAuthor().getId(), new JSONObject());
            database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).put("MissionID", postResponse.getString("result"));
            database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).put("StartTime", getCurrentTime());
            database.getJSONObject("mjByMissionID").put(postResponse.getString("result"), new JSONObject());
            database.getJSONObject("mjByMissionID").getJSONObject(postResponse.getString("result")).put("OwnerID", message.getAuthor().getId());
            database.getJSONObject("mjByMissionID").getJSONObject(postResponse.getString("result")).put("StartTime", getCurrentTime());

            channel.sendMessage("请输入 !mjget 或者 !mjget <任务ID> 查询图片结果/生成进度").queue();
        }

        if (content.equals("!mjget")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            if (database.getJSONObject("mj").has(message.getAuthor().getId())){
                String getResponse = GETRequest(mjUrl + "/mj/task/"+ database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getString("MissionID") +"/fetch", MidjourneyAPIKey, channel);
                JSONObject jsonRespond = null;
                jsonRespond = new JSONObject(getResponse);
                String pictureUrl = jsonRespond.getString("imageUrl");
                if (Objects.equals(pictureUrl, "")){

                    DateFormat bjDateFormat = new SimpleDateFormat(patterStr);
                    bjDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> 图片正在生成中: "+
                            "\n请求时间: " + bjDateFormat.format(database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getLong("StartTime")) +
                            "\n任务ID: " + database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getString("MissionID")
                    ).queue();
                } else {
                    channel.sendMessage("<@"+ message.getAuthor().getId() +">" + "[这是MidJourney生成的图片: ]("+ jsonRespond.getString("imageUrl") +")").queue();
                }
            } else {
                DateFormat bjDateFormat = new SimpleDateFormat(patterStr);
                bjDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                channel.sendMessage("你最近没有请求生成任何图片（上一次重启后: "+ bjDateFormat.format(serverStartTime) +"）").queue();
            }
        }

        if (content.startsWith("!mjget ")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            content = content.replaceFirst("!mjget ", "");
            JSONObject getRespond;
            try {
                getRespond = new JSONObject(Objects.requireNonNull(GETRequest(mjUrl + "/mj/task/" + content + "/fetch", MidjourneyAPIKey, channel)));
            } catch (Exception e){
                channel.sendMessage("向MidJourney发出GET请求时出现了问题").queue();
                return;
            }

            String pictureUrl = getRespond.getString("imageUrl");
            if (Objects.equals(pictureUrl, "")){
                if (database.getJSONObject("mjByMissionID").has(content)){
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> 图片正在生成中: "+
                            "\n请求时间: " + database.getJSONObject("mjByMissionID").getJSONObject(content).getString("StartTime")+
                            "\n发出请求的用户: <@" + database.getJSONObject("mjByMissionID").getJSONObject(content).getString("OwnerID") + ">"
                    ).queue();
                } else {
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> 该图片正在生成中...").queue();
                }

            } else {
                channel.sendMessage("<@"+ message.getAuthor().getId() +">" + "[这是MidJourney生成的图片: ]("+ getRespond.getString("imageUrl") +")").queue();
            }

        }

        if (content.startsWith("!ideogram ") || content.startsWith("!idg ")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (content.startsWith("!ideogram ")) content = content.replaceFirst("!ideogram ", "");
            if (content.startsWith("!idg ")) content = content.replaceFirst("!idg ", "");

            channel.sendMessage("图片正在生成中...").queue();
            channel.sendMessage("提示词：" + content).queue();

            String request = POSTRequest(ideogramUrl + "/generate", getIdeogramRequestBody(content).toString(), IdeogramAPIKey, channel);
            JSONObject jsonResponse = new JSONObject(request);

            channel.sendMessage("<@" + message.getAuthor().getId() + "> "+"[这是生成的图片: ]("+
                    jsonResponse
                            .getJSONArray("data")
                            .getJSONObject(0)
                            .getString("url") +")")
                    .queue();
        }

        // Music Generation
        if (content.startsWith("!suno ")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            content = content.replaceFirst("!", "");
            String[] args = content.split("\\s+");

            if (Objects.equals(args[1], "lyrics")){
                if (Objects.equals(args[2], "create")){
                    if (args[3].isEmpty()){
                        channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                        return;
                    }

                    String respond = POSTRequest(sunoUrl + "/generate/lyrics", "{\"prompt\":\"" + args[3] + "\"}", SunoAPIKey, channel);

                    if (respond == null){
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(respond);
                    channel.sendMessage("<@" + message.getAuthor().getId() + "> 这是歌词ID :" + jsonResponse.getString("id")).queue();
                    channel.sendMessage("如果想要获得歌词内容，请发送!suno lyrics get <歌词ID>").queue();
                    return;
                } else if (Objects.equals(args[2], "get")){
                    if (args[3].isEmpty()){
                        channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                        return;
                    }

                    String respond = GETRequest(sunoUrl + "/lyrics/" + args[3], SunoAPIKey, channel);

                    if (respond == null){
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(respond);

                    if (jsonResponse.has("error")){
                        channel.sendMessage(jsonResponse.getJSONObject("error").getString("message") + "!").queue();
                        return;
                    }

                    if (!Objects.equals(jsonResponse.getString("status"), "complete")){
                        channel.sendMessage("歌词未生成完毕！").queue();
                        return;
                    }

                    channel.sendMessage("<@" + message.getAuthor().getId() +"> 这是你生成的歌词：").queue();
                    channel.sendMessage(jsonResponse.getString("text")).queue();
                }
            }
            else if (Objects.equals(args[1], "create")){
                UUID requestUUID = UUID.randomUUID();
                database.getJSONObject("suno").getJSONObject("request").put(requestUUID.toString(), new JSONObject());
                database.getJSONObject("suno").getJSONObject("request").getJSONObject(requestUUID.toString()).put("prompt", "");
                database.getJSONObject("suno").getJSONObject("request").getJSONObject(requestUUID.toString()).put("title", "");
                database.getJSONObject("suno").getJSONObject("request").getJSONObject(requestUUID.toString()).put("mv", "chirp-v3-5");
                database.getJSONObject("suno").getJSONObject("request").getJSONObject(requestUUID.toString()).put("tags", "");

                channel.sendMessage("<@" + message.getAuthor().getId() + "> 已创建新的待生成空白歌曲，请设置后再发送请求！").queue();
                channel.sendMessage("待生成歌曲UUID: " + requestUUID).queue();
            }
            else if (Objects.equals(args[1], "set")){
                if (Objects.equals(args[2], "title")){
                    if (args[3].isEmpty() || args[4].isEmpty()){
                        channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                        return;
                    }

                    if (!database.getJSONObject("suno").getJSONObject("request").has(args[3])){
                        channel.sendMessage("未寻找到该歌曲任务！").queue();
                        return;
                    }

                    database.getJSONObject("suno").getJSONObject("request").getJSONObject(args[3]).put("title", args[4]);
                    channel.sendMessage("歌曲任务标题设置成功！").queue();
                }
                if (Objects.equals(args[2], "lyrics")){
                    if (args[3].isEmpty() || args[4].isEmpty()){
                        channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                        return;
                    }

                    if (!database.getJSONObject("suno").getJSONObject("request").has(args[3])){
                        channel.sendMessage("未寻找到该歌曲任务！").queue();
                        return;
                    }

                    String respond = GETRequest(sunoUrl + "/lyrics/" + args[4], SunoAPIKey, channel);

                    if (respond == null){
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(respond);

                    if (jsonResponse.has("error")){
                        channel.sendMessage(jsonResponse.getJSONObject("error").getString("message") + "!").queue();
                        return;
                    }

                    if (!Objects.equals(jsonResponse.getString("status"), "complete")){
                        channel.sendMessage("歌词未生成完毕！").queue();
                        return;
                    }

                    database.getJSONObject("suno").getJSONObject("request").getJSONObject(args[3]).put("prompt", jsonResponse.getString("text"));

                    channel.sendMessage("歌曲任务歌词设置成功！").queue();

                }
                if (Objects.equals(args[2], "tags")){
                    if (args[3].isEmpty() || args[4].isEmpty()){
                        channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                        return;
                    }

                    if (!database.getJSONObject("suno").getJSONObject("request").has(args[3])){
                        channel.sendMessage("未寻找到该歌曲任务！").queue();
                        return;
                    }

                    database.getJSONObject("suno").getJSONObject("request").getJSONObject(args[3]).put("tags", args[4]);
                    channel.sendMessage("歌曲任务曲风设置成功！").queue();
                }
            }
            else if (Objects.equals(args[1], "generate")){
                if (args[2].isEmpty()){
                    channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                    return;
                }

                if (!database.getJSONObject("suno").getJSONObject("request").has(args[2])){
                    channel.sendMessage("未寻找到该歌曲任务！").queue();
                    return;
                }

                String respond = POSTRequest(sunoUrl + "/generate", database.getJSONObject("suno").getJSONObject("request").getJSONObject(args[2]).toString(), SunoAPIKey, channel);
                if (respond == null){
                    return;
                }
                JSONObject jsonResponse = new JSONObject(respond);
                channel.sendMessage("<@"+ message.getAuthor().getId() +">这是你生成的最终歌曲ID (共两首)：").queue();
                channel.sendMessage("第一首: " + jsonResponse.getJSONArray("clips").getJSONObject(0).getString("id")).queue();
                channel.sendMessage("第二首: " + jsonResponse.getJSONArray("clips").getJSONObject(1).getString("id")).queue();
            }
            else if (Objects.equals(args[1], "get")){

                if (args[2].isEmpty()){
                    channel.sendMessage("指令使用错误，请使用/suno 指令查看suno可用的指令").queue();
                    return;
                }

                String respond = GETRequest(sunoUrl + "/feed/" + args[2], SunoAPIKey, channel);
                if (respond == null){
                    return;
                }

                JSONArray jsonResponse = new JSONArray(respond);
                channel.sendMessage("<@" + message.getAuthor().getId() + "> 这是生成结果：\n"+
                        "## 歌曲名称: " + jsonResponse.getJSONObject(0).getString("title") + "\n"+
                        "歌曲链接: [点击我听歌]("+ jsonResponse.getJSONObject(0).getString("audio_url") +")\n"+
                        "大封面: [点击查看]("+ jsonResponse.getJSONObject(0).getString("image_large_url") +")\n"+
                        "## 歌词: \n"+
                        jsonResponse.getJSONObject(0).getJSONObject("metadata").getString("prompt") + "\n"+
                        "## 封面："
                ).queue();
            }

        }

        if (content.equals("!newconversation") || content.equals("!nc")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            if (catPromptEnable){
                database.getJSONObject("CatChatHistory").put(message.getAuthor().getId(), new JSONArray());
                channel.sendMessage("<@" + message.getAuthor().getId() + "> 已开启新的猫娘聊天对话！").queue();
            } else {
                database.getJSONObject("ChatHistory").put(message.getAuthor().getId(), new JSONArray());
                channel.sendMessage("<@" + message.getAuthor().getId() + "> 已开启新的ChatGPT聊天对话！").queue();
            }
        }

        if (content.equals("!aethersversion")) {
            channel.sendMessage(
                    "版本 1.3.2\n"+
                            "问题修复\n"+
                            "过往版本更新：\n"+
                            "当文本长度大于2000时发送回复txt文件\n"+
                            "增加了Suno音乐生成AI\n"+
                            "增加了新的图片生成模型：ideogram, 输入 !idg <提示词> 即可调用\n" +
                            "输入/help 即可获取完整指令列表"
                    ).queue();
        }
    }

    public String POSTRequest(String url, String body, String apiKey, MessageChannel channel) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            // 设置 url
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);

            // 设置请求 body
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(connection.getOutputStream());
            // 保存body
            out.print(body);
            // 发送body
            out.flush();
            // 获取响应body
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            channel.sendMessage("在向API请求数据时发生错误，请联系Bot管理员!").queue();;
            System.err.println("ERROR 在向API请求数据时发生错误");
            e.printStackTrace();
            return null;
        }

        return result.toString();
    }

    public static String GETRequest(String url, String apiKey, MessageChannel channel){
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            // 建立实际的连接
            connection.connect();
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            channel.sendMessage("在发送GET请求时发生错误，请联系Bot管理员!").queue();
            System.err.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
            return null;
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result.toString();
    }

    public JSONObject getOpenAIChatRequestBody(String sendContent, String prompt, JSONArray chatHistory){

        JSONObject inputJSON = new JSONObject();
        if (!catPromptEnable) inputJSON.put("model", model);
        else inputJSON.put("model", catModel);

        inputJSON.put("max_tokens", 1200);
        inputJSON.put("temperature", 0.8);

        JSONArray messages = new JSONArray();
        JSONObject user = new JSONObject();
        JSONObject system = new JSONObject();
        JSONObject sys2 = new JSONObject();

        system.put("role", "system");
        if (!catPromptEnable){
            system.put("content", prompt);
        } else {
            system.put("content", catPrompt);
            sys2.put("role", "system");
            sys2.put("content", catPrompt2);
        }

        user.put("role", "user");
        user.put("content", sendContent);

        messages.put(system);
        if (catPromptEnable) messages.put(sys2);

        if (!chatHistory.isEmpty()){
            for (Object jsObj : chatHistory){
                messages.put(jsObj);
            }
        }
        messages.put(user);
        inputJSON.put("messages", messages);

        return inputJSON;

    }

    public JSONObject getOpenAIImageRequestBody(String prompt){

        JSONObject inputJSON = new JSONObject();

        inputJSON.put("model", "dall-e-3");
        inputJSON.put("prompt", prompt);
        inputJSON.put("n", 1);
        inputJSON.put("size", "1024x1024");

        return inputJSON;

    }

    public JSONObject getModJourneyRequestBody(String prompt){

        JSONObject inputJSON = new JSONObject();

        inputJSON.put("base64Array", new JSONArray());
        inputJSON.put("instanceId", "");
        inputJSON.put("modes", new JSONArray());
        inputJSON.put("notifyHook", "https://ww.baidu.com/notifyHook/back");
        inputJSON.put("prompt", prompt);
        inputJSON.put("remix", true);
        inputJSON.put("state", "");

        return inputJSON;

    }

    public JSONObject getIdeogramRequestBody(String prompt){
        JSONObject output = new JSONObject();

        Random random = new Random();
        int seed = random.nextInt(2147483647);

        output.put("image_request", new JSONObject());

        output.getJSONObject("image_request").put("model", ideogramModel);
        output.getJSONObject("image_request").put("magic_prompt_option", "AUTO");
        output.getJSONObject("image_request").put("prompt", prompt);
        output.getJSONObject("image_request").put("aspect_ratio", "ASPECT_16_9");
        output.getJSONObject("image_request").put("seed", seed);
        
        return output;
    }

    public long getCurrentTime(){
        Date currentTimeMili = Calendar.getInstance(Locale.CHINA).getTime();
        return currentTimeMili.getTime();
    }

    public void setIsFixingBot(boolean type){
        isFixingBot = type;
    }

    public void setListeningChannel(String channelID){
        this.listeningChannelID = channelID;
    }

    public void longMessage(String message, MessageChannel channel, String senderID) throws IOException {
        File file = new File("./TempMessageFile.txt");

        if (file.exists()) {
            // 如果已存在,删除旧文件
            file.delete();
        }
        file.createNewFile();
        Writer write = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
        write.write(message);
        write.flush();
        write.close();

        FileUpload files = FileUpload.fromData(file, "message.txt");
        channel.sendMessage("<@" + senderID + ">").addFiles(files).queue();
        file.delete();
    }

    public void reloadConfig(JSONObject config){

        database.put("config", config);

        ChatGPTAPIKey = config.getJSONObject("AISetting").getString("ChatGPTAPIKey");
        MidjourneyAPIKey = config.getJSONObject("AISetting").getString("MidJourneyAPIKey");
        IdeogramAPIKey = config.getJSONObject("AISetting").getString("IdeogramAPIKey");
        SunoAPIKey = config.getJSONObject("AISetting").getString("SunoAPIKey");
        RolePlayAPIKey = config.getJSONObject("AISetting").getString("RolePlayGPTAPIKey");

        model = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("model");
        chatUrl = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("url");
        pictureUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("DALLE3").getString("url");
        listeningChannelID = config.getString("ListeningChannelID");
        mjUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("MidJourney").getString("url");
        prompt = config.getJSONObject("AISetting").getJSONObject("ChatGPT").getString("prompt");
        clientID = config.getJSONObject("discord").getString("clientID");
        catModel = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("model");
        catPrompt = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("prompt1");
        catPrompt2 = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("prompt2");
        catUrl = config.getJSONObject("AISetting").getJSONObject("CatChat").getString("url");
        maxHistory = config.getJSONObject("AISetting").getInt("MaxHistory");
        ideogramModel = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").getString("model");
        ideogramUrl = config.getJSONObject("AISetting").getJSONObject("PictureAI").getJSONObject("Ideogram").getString("url");
        sunoUrl = config.getJSONObject("suno").getString("url");

        whitelistedServer = new ArrayList<>();
        JSONArray wja = config.getJSONObject("discord").getJSONArray("whitelistedChannelID");
        for (int i = 0;i < wja.length();i++){
            whitelistedServer.add(wja.getString(i));
        }

    }
}
