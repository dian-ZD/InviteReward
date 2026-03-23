package io.Sriptirc_wp_989.invitereward;

import io.Sriptirc_wp_989.invitereward.command.MainCommandExecutor;
import io.Sriptirc_wp_989.invitereward.listener.PlayerJoinListener;
import io.Sriptirc_wp_989.invitereward.manager.ConfigManager;
import io.Sriptirc_wp_989.invitereward.manager.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Invitereward extends JavaPlugin {
    private DataManager dataManager;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        
        // 初始化管理器
        configManager = new ConfigManager(this);
        dataManager = new DataManager(this);
        
        // 初始化命令执行器
        MainCommandExecutor commandExecutor = new MainCommandExecutor(this, dataManager, configManager);
        
        // 注册命令
        getCommand("cinvc").setExecutor(commandExecutor);
        getCommand("cinvc").setTabCompleter(commandExecutor);
        
        getCommand("icode").setExecutor(commandExecutor);
        getCommand("icode").setTabCompleter(commandExecutor);
        
        getCommand("invc").setExecutor(commandExecutor);
        getCommand("invc").setTabCompleter(commandExecutor);
        
        getCommand("inv").setExecutor(commandExecutor);
        getCommand("inv").setTabCompleter(commandExecutor);
        
        // 注册事件监听器
        PlayerJoinListener joinListener = new PlayerJoinListener(this, dataManager, configManager);
        getServer().getPluginManager().registerEvents(joinListener, this);
        
        getLogger().info("邀请奖励系统已启用！");
        getLogger().info("游戏版本：1.21.8");
        getLogger().info("插件版本：" + getDescription().getVersion());
    }
    
    @Override
    public void onDisable() {
        // 保存数据
        if (dataManager != null) {
            dataManager.save();
        }
        
        getLogger().info("邀请奖励系统已禁用。");
    }
    
    /**
     * 获取数据管理器（供其他类使用）
     */
    public DataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * 获取配置管理器（供其他类使用）
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
