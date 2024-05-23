package com.puddingkc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ReactionTest extends JavaPlugin implements Listener, CommandExecutor {

    private final Map<UUID, Long> playerReactionTimes = new HashMap<>();
    private final List<UUID> playerTesting = new ArrayList<>();
    private final Random random = new Random();
    private static final long maxWaitTime = 5000L;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("reactiontest")).setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (playerTesting.contains(player.getUniqueId())) {
                player.sendMessage("§e反应测试 §8| §c你已经开始一轮测试了，请等待测试结束");
                return false;
            }
            startReactionTest(player);
            return true;
        }
        return false;
    }

    private void startReactionTest(Player player) {
        playerTesting.add(player.getUniqueId());
        player.sendMessage("§e反应测试 §8| §7请在出现 §a开始 §7字样后快速按下 §fShift §7下蹲");

        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendTitle( ChatColor.YELLOW + "预备", "", 0, 40, 0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendTitle(ChatColor.GREEN + "开始", "", 0, 40, 0);
                        long startTime = System.currentTimeMillis();
                        playerReactionTimes.put(player.getUniqueId(), startTime);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (playerReactionTimes.containsKey(player.getUniqueId())) {
                                    playerReactionTimes.remove(player.getUniqueId());
                                    playerTesting.remove(player.getUniqueId());
                                    player.sendMessage(ChatColor.RED + "§e反应测试 §8| §c反应超时，请重新测试");
                                }
                            }
                        }.runTaskLater(ReactionTest.this, maxWaitTime / 50);
                    }
                }.runTaskLater(ReactionTest.this, 60L + random.nextInt(141));
            }
        }.runTaskLater(this, 45);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (playerReactionTimes.containsKey(player.getUniqueId())) {
            long reactionStartTime = playerReactionTimes.remove(player.getUniqueId());
            long reactionTime = System.currentTimeMillis() - reactionStartTime;
            player.sendMessage("§e反应测试 §8| §7你的反应时间为: §a" + reactionTime + " ms");
            player.sendTitle( ChatColor.GREEN + String.valueOf(reactionTime) + " ms" ,"",0,60,0);
            playerTesting.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerReactionTimes.remove(player.getUniqueId());
        playerTesting.remove(player.getUniqueId());
    }
}
