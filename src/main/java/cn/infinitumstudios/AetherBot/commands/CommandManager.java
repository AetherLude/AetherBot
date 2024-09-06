package cn.infinitumstudios.AetherBot.commands;

import cn.infinitumstudios.AetherBot.listeners.MessageListener;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    MessageListener ml;
    JSONObject config;
    public CommandManager(MessageListener ml, JSONObject config){
        this.ml = ml;
        this.config = config;
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("help", "Aethers深井机器人可用指令的菜单"));
        commandData.add(Commands.slash("suno", "SunoAI音乐合成指令菜单"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("help", "Aethers深井机器人可用指令的菜单"));
        commandData.add(Commands.slash("suno", "SunoAI音乐合成指令菜单"));
        event.getGuild().updateCommands().addCommands(commandData).queue();

    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("help")){
            event.reply("<@" + event.getUser().getId() +"> \n"+
                    "这是 Aethers深井机器人 的指令菜单\n"+
                    "!suno - Suno AI 合成音乐，请使用指令/suno 获取Suno指令菜单\n"+
                    "!id - 获取该频道的id\n"+
                    "!ping - 获取机器人是否在线 (如果不在线将不会发送信息)\n"+
                    "!chat | !c | <@" + config.getJSONObject("discord").getString("clientID") +"> - 开启 ChatGPT AI聊天\n"+
                    "!newconversation | !nc - 开启新的会话，GPT聊天历史记录将被移除\n"+
                    "!cat - 启动/关闭猫娘模式 (启用后只能用 !c | !chat 呼唤，启动后将无法正常询问问题，只能互动！)\n"+
                    "!dalle3 <提示词> | !d3 <提示词> - 使用 DALL·E 3 生成一张图片 (自动返回结果)\n"+
                    "!idj <提示词> | !ideogram <提示词> 使用Ideogram生成一张图片 (自动返回结果)\n"+
                    "!midjourney <提示词> | !mj <提示词> - 使用 MidJourney 生成一张图片 (需要手动查询结果)\n"+
                    "!mjget - 查询最近一次MidJourney生成的图片\n"+
                    "!mjget <任务id> - 根据任务ID查询生成的图片\n"+
                    "!aethersversion - 查看当前机器人版本\n"+
                    "提示: !ping 还能知道目前猫娘模式是否开启哦～"
            ).queue();
        }

        if (command.equals("suno")){
            event.reply("<@" + event.getUser().getId() +"> \n"+
                    "这是 SunoAI音乐合成 指令菜单，如有问题请询问AetherLude!\n"+
                    "!suno lyrics create <提示词> 生成一个歌词，获取歌词ID\n" +
                    "!suno lyrics get <ID> 根据歌词ID获取生成的歌词\n" +
                    "!suno create 创建空白歌曲任务，获取歌曲任务UUID\n" +
                    "!suno set title <UUID> <名称> 设置歌曲的名称\n" +
                    "!suno set lyrics <UUID> <歌词ID> 设置歌曲的歌词\n" +
                    "!suno set tags <UUID> <歌曲曲风> 设置歌曲的曲风\n" +
                    "!suno generate <UUID> 开始生成歌曲 (会返回两首歌以及歌曲ID)\n" +
                    "!suno get <歌曲ID> 获取生成的歌\n" +
                    "使用步骤：\n" +
                    "1. 生成一个歌词，想好歌曲的标题和曲风\n" +
                    "2. 通过 suno create 指令创建一个空白歌曲任务\n" +
                    "3. 通过 suno set title | lyrics | tags 设置歌曲任务\n" +
                    "注意，title为设置标题，lyrics为设置歌词 (请填歌词的ID)，tags为曲风(千万不要在不同曲风之间加空格! 程序无法识别!)\n"+
                    "4. 通过 suno generate <歌曲任务UUID> 生成歌曲，这会生成两首歌，可以获取任意一首歌\n" +
                    "5. 通过 suno get <歌曲ID> 获取生成的歌"
            ).queue();
        }
    }

    public void reloadConfig(JSONObject config){
        this.config = config;
    }
}
