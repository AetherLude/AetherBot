package cn.infinitumstudios.AetherBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommandListener extends ListenerAdapter
{
    MessageChannel channel;
    String apiKey, model, chatUrl, pictureUrl, mjUrl, prompt, clientID, r18ChannelID, catModel;
    String r18CatPromptP1, r18CatPromptP2, r18CatPromptP3, r18CatPromptP4;
    List<String> whitelistedServer;
    JSONObject database;
    String patterStr = "yyyy-MM-dd HH:mm:ss";
    protected boolean isFixingBot = false;
    Date serverStartTime;
    boolean catPromptEnable;
    public CommandListener(String apiKey, String model, String url, String mjUrl, String clientID, @Nullable String prompt, String catModel, Date serverStartTime){
        this.apiKey = apiKey;
        this.model = model;
        this.chatUrl = url + "/chat/completions";
        this.pictureUrl = url + "/images/generations";
        this.prompt = prompt;
        this.clientID = clientID;
        this.catModel = catModel;
        this.mjUrl = mjUrl + "/fast";
        this.serverStartTime = serverStartTime;
        database = new JSONObject();
        database.put("mj", new JSONObject());
        database.put("mjByMissionID", new JSONObject());
        catPromptEnable = false;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        channel = event.getChannel();

        if (!whitelistedServer.contains(channel.getId())){
            return;
        }

        Message message = event.getMessage();
        String content = message.getContentRaw();

        if (event.getAuthor().getName().equals("Aethers深井机器人")) return;

        if (event.getAuthor().isBot() && !content.equals("我上早八")) return;

        if (content.equals("!id")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            channel.sendMessage(channel.getId()).queue();
        }

        if (content.equals("!cat")){

            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if (catPromptEnable){
                catPromptEnable = false;
                channel.sendMessage("猫娘模式关闭！").queue();
                return;
            }

            catPromptEnable = true;
            channel.sendMessage("猫粮模式(只r18频道可用)启动！").queue();
            return;
        }

        if (content.equals("!ping")) {
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            if (catPromptEnable){
                channel.sendMessage("Pong！Aether猫粮机器人在线！").queue();
            } else {
                channel.sendMessage("Pong！Aether深井机器人在线！").queue();
            }
            // Important to call .queue() on the RestAction returned by sendMessage(...)
        }

        if (content.equals("TD")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("已退订该消息").queue();
        }

        if (content.equals("R")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("已拒收该消息").queue();
        }

        if (content.equals("我上早八")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("666").queue();
        }

        if (content.equals("6")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("9").queue();
        }

        if (content.equals("666")){
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                return;
            }
            channel.sendMessage("999").queue();
        }

        if (content.startsWith("!testconnection") || content.startsWith("!tc")) {
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            content = "testing connection";
            String respond = openAIAPIPostRequest(chatUrl, getOpenAIChatRequestBody(content,message,prompt).toString(),apiKey,channel);
            if (respond == null) {
                channel.sendMessage("测试-连接OpenAI API失败！").queue();
            } else {
                channel.sendMessage("测试-连接OpenAI API成功！").queue();
            }

        }

        if (content.startsWith("!chat ") || content.startsWith("!c ")) {
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            MessageChannel channel = event.getChannel();

            if (content.startsWith("!chat ")) content = content.replaceFirst("!chat ", "");
            if (content.startsWith("!c ")) content = content.replaceFirst("!c ", "");

            String reply;
            // https请求
            if (!catPromptEnable) reply = openAIAPIPostRequest(chatUrl, getOpenAIChatRequestBody(content,message,prompt).toString(),apiKey,channel);
            else if (channel.getId().equals(r18ChannelID) || channel.getId().equals("1164928327686037534")){
                reply = openAIAPIPostRequest(chatUrl, getR18CatPromptBody(content, message).toString(),apiKey,channel);
            } else {
                channel.sendMessage("猫娘模式已开启，请在年龄限制文字频道使用！").queue();
                channel.sendMessage("输入!cat 可以关闭猫娘模式").queue();
                return;
            }

            if (reply == null) {
                channel.sendMessage("出现内部错误，指令执行失败").queue();
                System.out.println("无效的返回值");
                return;
            }

            try {
                JSONObject replyJSON = new JSONObject(reply);

                String output = replyJSON
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                if (output == null){
                    channel.sendMessage("出现内部错误，指令执行失败,line170").queue();
                    return;
                }

                channel.sendMessage("<@"+ message.getAuthor().getId() +"> " + output).queue();
                System.out.println("Bot.ReplyMessage: " + output);

            } catch (Exception e) {
                e.printStackTrace();
                channel.sendMessage("出现内部错误，指令执行失败,line179").queue();
            }
        }

        if (content.contains("Aethers深井机器人") || content.contains("<@"+ clientID +">")){

            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }

            if(content.contains("<@"+ clientID +">"))content = content.replaceAll("<@"+ clientID +">", "@Aethers深井机器人 ");

            // Post
            String reply = null;

            if (!catPromptEnable) reply = openAIAPIPostRequest(chatUrl, getOpenAIChatRequestBody(content,message,prompt).toString(),apiKey,channel);
            else {
                channel.sendMessage("猫娘模式只能通过 !c / !chat 开启对话，无法使用@").queue();
                return;
            }

            if (reply == null) return;

            System.out.println(reply);

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

                // result
                channel.sendMessage("<@"+ message.getAuthor().getId() +"> " + output).queue();
                System.out.println("Bot.ReplyMessage: " + output);

            } catch (Exception e) {
                e.printStackTrace();
                channel.sendMessage("出现内部错误，指令执行失败").queue();
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
                String respond = openAIAPIPostRequest(pictureUrl, getOpenAIImageRequestBody(content).toString(), apiKey, channel);
                if (respond == null){
                    channel.sendMessage("出现内部错误，指令执行失败(respond is null,line198)").queue();
                    return;
                }
                JSONObject respondJSON = new JSONObject(respond);
                String pictureURL = respondJSON
                        .getJSONArray("data")
                        .getJSONObject(0)
                        .getString("url");
                channel.sendMessage("<@"+message.getAuthor().getId()+"> [这是生成的图片: ](" + pictureURL + ")").queue();
            } catch (Exception e){
                channel.sendMessage("出现内部错误，指令执行失败(Respond -> JSON, line208)").queue();
                e.printStackTrace();
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
            String postReply = openAIAPIPostRequest(mjUrl + "/mj/submit/imagine", getModJourneyRequestBody(content).toString(), apiKey, channel);
            if (postReply == null){
                channel.sendMessage("出现内部错误，指令执行失败(Post,line229)").queue();
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
                String getResponse = RESTAPIGetRequest(mjUrl + "/mj/task/"+ database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getString("MissionID") +"/fetch", apiKey, channel);
                JSONObject jsonRespond = null;
                if (getResponse == null){
                    channel.sendMessage("出现内部错误，指令执行失败(GET,line246)").queue();
                    return;
                } else {
                    jsonRespond = new JSONObject(getResponse);
                }
                String pictureUrl = jsonRespond.getString("imageUrl");
                if (Objects.equals(pictureUrl, "")){
                    channel.sendMessage("<@"+ message.getAuthor().getId() +"> 图片正在生成中: "+
                            "\n请求时间: " + database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getString("MissionID") +
                            "\n任务ID: " + database.getJSONObject("mj").getJSONObject(message.getAuthor().getId()).getString("StartTime")
                    ).queue();
                } else {
                    channel.sendMessage("<@"+ message.getAuthor().getId() +">" + "[这是MidJourney生成的图片: ]("+ jsonRespond.getString("imageUrl") +")").queue();
                }
            } else {
                DateFormat bjDateFormat = new SimpleDateFormat(patterStr);
                bjDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                channel.sendMessage("你最近没有请求生成任何图片（上一次重启后: "+ bjDateFormat.format(serverStartTime.getTime()) +"）").queue();
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
                getRespond = new JSONObject(RESTAPIGetRequest(mjUrl + "/mj/task/"+ content +"/fetch", apiKey, channel));
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

        if (content.equals("!aethersversion")) {
            if (!message.getAuthor().getName().equals("alpinecore_") && isFixingBot){
                channel.sendMessage("此机器人正在调试中，无法使用").queue();
                return;
            }
            channel.sendMessage(
                    "版本 1.1.5\n"+
                            "该版本为BUG修复和优化\n" +
                            "新增了 !help 指令菜单\n"+
                            "1.1.0至该版本主要更新内容：\n"+
                            "1. 增加聊天频道白名单设置（防止滥用）\n"+
                            "2. !mjget - 可以获取你最近一次的MidJourney生成图片\n"+
                            "3. 猫娘Prompt (需在年龄限制频道使用)，可输入\"!cat\"进行切换\n"+
                            "4. 新增AI画图 - !dalle3 提示词 (!d3 提示词)\n"+
                            "5. 新增AI画图 - !midjourney 提示词 (!mj 提示词)"
                    ).queue();
        }



        if (content.equals("!help")){
            channel.sendMessage(
                    "<@" + message.getAuthor().getId() +"> \n"+
                    "这是 Aethers深井机器人 的指令菜单\n"+
                    "!id - 获取该频道的id\n"+
                    "!ping - 获取机器人是否在线 (如果不在线将不会发送信息)\n"+
                    "!testconnection - 测试连接至AI API\n"+
                    "!chat | !c | <@" + clientID +"> - 开启 ChatGPT/Claude-3 AI聊天\n"+
                    "!cat - 启动/关闭猫娘模式 (启用后只能在年龄限制频道使用，并且只能用 !c | !chat 呼唤)\n"+
                    "!dalle3 <提示词> | !d3 <提示词> - 使用 DALL·E 3 生成一张图片 (自动返回生成结果)\n"+
                    "!midjourney <提示词> | !mj <提示词> - 使用 MidJourney 生成一张图片 (需要手动查询结果)\n"+
                    "!mjget - 查询最近一次MidJourney生成的图片\n"+
                    "!mjget <任务id> - 根据任务ID查询生成的图片\n"+
                    "!aethersversion - 查看当前机器人版本\n"+
                    "提示: !ping 还能知道目前猫娘模式是否开启哦～"
            ).queue();
        }
    }

    public static String openAIAPIPostRequest(String url, String body, String apiKey, MessageChannel channel) {
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
            channel.sendMessage("在向Open AI API请求数据时发生错误，请联系Bot管理员!").queue();;
            System.out.println("ERROR 在向Open AI API请求数据时发生错误");
            e.printStackTrace();
            return null;
        }

        return result.toString();
    }

    public static String RESTAPIGetRequest(String url, String apiKey, MessageChannel channel){
        String result = "";
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
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            channel.sendMessage("在发送GET请求时发生错误，请联系Bot管理员!").queue();
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
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
        return result;

    }

    public JSONObject getOpenAIChatRequestBody(String sendContent, Message message, String prompt){

        JSONObject inputJSON = new JSONObject();

        if (catPromptEnable) inputJSON.put("model", catModel);
        else inputJSON.put("model", model);

        inputJSON.put("max_tokens", 1200);
        inputJSON.put("temperature", 0.8);

        JSONArray messages = new JSONArray();
        JSONObject user = new JSONObject();
        JSONObject system = new JSONObject();

        user.put("role", "user");
        user.put("content", "来自用户\""+ message.getAuthor().getName() +"\"的信息: " +sendContent);

        system.put("role", "system");
        system.put("content", prompt);

        messages.put(system);
        messages.put(user);

        inputJSON.put("messages", messages);

        return inputJSON;

    }

    public JSONObject getR18CatPromptBody(String sendContent, Message message){

        JSONObject inputJSON = new JSONObject();

        inputJSON.put("model", model);
        inputJSON.put("max_tokens", 1200);
        inputJSON.put("temperature", 0.8);

        JSONArray messages = new JSONArray();
        JSONObject user = new JSONObject();
        JSONObject system1 = new JSONObject();
        JSONObject system2 = new JSONObject();
        JSONObject system3 = new JSONObject();
        JSONObject system4 = new JSONObject();

        system1.put("role", "system");
        system1.put("content", r18CatPromptP1);

        system2.put("role", "system");
        system2.put("content", r18CatPromptP2);

        system3.put("role", "system");
        system3.put("content", r18CatPromptP3);

        system4.put("role", "system");
        system4.put("content", r18CatPromptP4);

        user.put("role", "user");
        user.put("content", "来自用户\""+ message.getAuthor().getName() +"\"的信息: " +sendContent);

        messages.put(system1);
        messages.put(system2);
        messages.put(system3);
        messages.put(system4);

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

    public String getCurrentTime(){
        Date currentTimeMili = Calendar.getInstance(Locale.CHINA).getTime();
        DateFormat bjDateFormat = new SimpleDateFormat(patterStr);
        bjDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return bjDateFormat.format(currentTimeMili.getTime());
    }

    public void setIsFixingBot(boolean type){
        isFixingBot = type;
    }

    public void setWhitelistChannel(List<String> channelIDList, String r18ChannelID){
        this.whitelistedServer = channelIDList;
        this.r18ChannelID = r18ChannelID;
    }

    public void setR18CatPrompt(String part1, String part2, String part3, String part4){
        r18CatPromptP1 = part1;
        r18CatPromptP2 = part2;
        r18CatPromptP3 = part3;
        r18CatPromptP4 = part4;
    }
}
