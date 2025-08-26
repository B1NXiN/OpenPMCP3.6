package pmcp.command;

import pmcp.command.mode.*;
import pmcp.event.EventTarget;
import pmcp.event.mode.client.EventChat;
import pmcp.utils.client.Helper;
import pmcp.utils.client.MinecraftInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager extends MinecraftInstance {
    private final List<Command> commands = new ArrayList<>();

    public void init() {
        commands.add(new Chat());
        commands.add(new Toggle());
        commands.add(new Bind());
        commands.add(new Config());
        commands.add(new Binds());
        commands.add(new Friend());
        commands.add(new FakeName());
        commands.add(new IRCChat());
        commands.add(new IRCKick());
    }

    private Command getCommandByName(String name) {
        return commands.stream()
                .filter(cmd -> cmd.getName().toLowerCase().equals(name))
                .findFirst()
                .orElse(null);
    }

    private List<String> getCommandSuggestions(String input) {
        return commands.stream()
                .map(Command::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    @EventTarget
    private void onChat(EventChat event) {
        String message = event.getMessage();
        if (!message.startsWith(".")) {
            return;
        }

        event.cancelEvent();

        String[] args = message.trim().substring(1).split(" ");
        String commandName = args[0].toLowerCase();
        Command command = getCommandByName(commandName);

        if (args[0].isEmpty()) {
            Helper.sendMessage("Command","请勿输入空指令！");
            return;
        }

        if (args.length == 1 && !isValidCommand(commandName)) {
            List<String> suggestions = getCommandSuggestions(commandName);
            suggestions.add("help");
            Helper.sendMessage("Command"," '" + commandName + "' 无效！可使用命令为：[ " + String.join("| ", suggestions) +"]");
            return;
        }

        if (commandName.equals("help")) {
            sendHelpMessages();
            return;
        }

        if (command != null) {
            command.execute(Arrays.copyOfRange(args, 1, args.length));
        } else {
            Helper.sendMessage("Command","未知命令: " + commandName);
        }
    }

    private boolean isValidCommand(String commandName) {
        if ("help".equalsIgnoreCase(commandName)) {
            return true;
        }

        for (Command cmd : commands) {
            String cmdName = cmd.getName();

            if (commandName.equalsIgnoreCase(cmdName)) {
                return true;
            }

            if (!commandName.isEmpty() && !cmdName.isEmpty() &&
                    commandName.substring(0, 1).equalsIgnoreCase(cmdName.substring(0, 1))) {
                return true;
            }
        }

        return false;
    }

    private void sendHelpMessages() {
        Helper.sendMessage("Command","指令用法如下：");
        Helper.sendMessage("Command",".bind 模块  ，设置模块的按键绑定");
        Helper.sendMessage("Command",".binds  ，查询所有按键绑定");
        Helper.sendMessage("Command",".toggle 模块  ，打开/关闭模块");
        Helper.sendMessage("Command",".chat 文本  ，输出一段文本");
        Helper.sendMessage("Command",".friend add/remove/list  ，添加、删除、查询好友");
        Helper.sendMessage("Command",".config load/save/list 文本  ，登录、保存、查看配置");
        Helper.sendMessage("Command",".i <message>, 和同样使用PMCP的黑客聊天");
    }
}
