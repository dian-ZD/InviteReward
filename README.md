# InviteReward - Minecraft 邀请奖励系统

一个为 Minecraft 1.21.8 服务器设计的邀请奖励插件，允许玩家生成邀请码邀请其他玩家加入，双方都可以获得奖励。

## 功能特性

- **生成邀请码**：玩家输入 `/cinvc` 生成唯一邀请码（格式：XXXX-XXXX）
- **使用邀请码**：被邀请者输入 `/icode <邀请码>` 或 `/invc <邀请码>` 使用邀请码
- **邀请统计**：管理员输入 `/inv` 查看所有邀请者的邀请次数统计
- **智能奖励系统**：
  - 邀请者在线：立即发放双方奖励
  - 邀请者离线：奖励加入队列，待其上线后自动发放
  - 可配置奖励指令，支持变量替换
- **防滥用机制**：
  - 禁止邀请者使用自己的邀请码
  - 防止玩家重复使用邀请码（可选）
  - 邀请码唯一性保障

## 命令列表

| 命令 | 权限 | 描述 |
|------|------|------|
| `/cinvc` | `invite.use` (可选) | 生成一个新的邀请码，如果已有则替换旧码 |
| `/icode <邀请码>` | `invite.use` (可选) | 使用邀请码 |
| `/invc <邀请码>` | `invite.use` (可选) | 使用邀请码（与 `/icode` 功能相同） |
| `/inv` | `invite.admin` | 查看所有邀请者的邀请统计 |

## 权限节点

- `invite.use` - 允许生成和使用邀请码（默认：OP）
- `invite.admin` - 允许查看邀请统计（默认：OP）

权限可在 `config.yml` 中配置是否启用检查。

## 配置说明

插件首次运行会自动生成 `config.yml` 文件，包含以下可配置项：

### 奖励指令
```yaml
reward-commands:
  - "give %inviter% diamond 1"
  - "give %invitee% iron_ingot 5"
```

可用变量：`%inviter%` (邀请者), `%invitee%` (被邀请者)

### 邀请码设置
```yaml
invite-code:
  allow-multiple-uses: true      # 是否允许多人使用同一个邀请码
  prevent-self-use: true         # 是否禁止邀请者使用自己的邀请码
  prevent-player-reuse: true     # 是否防止玩家多次使用邀请码
```

### 奖励执行设置
```yaml
reward:
  offline-handling: "queue"      # queue:队列等待, skip_inviter:跳过邀请者, execute_now:立即执行
  queue-max-wait: 1440           # 队列等待最大时间（分钟），0为无限等待
```

### 权限设置
```yaml
permissions:
  require-invite-use: false      # 生成邀请码是否需要权限
  require-use-code: false        # 使用邀请码是否需要权限
  require-view-stats: true       # 查看统计是否需要权限
```

### 消息自定义
所有游戏内消息均可自定义，支持颜色代码（&）。

## 安装与使用

1. **安装插件**：
   - 将插件导出为 `.sirc` 文件
   - 放入 `plugins/ScriptIrc/scripts/src/` 目录
   - 在游戏内或控制台执行 `/scriptirc compiler InviteReward`
   - 编译成功后重启服务器

2. **玩家使用**：
   - 邀请者输入 `/cinvc` 生成邀请码
   - 将邀请码分享给其他玩家
   - 被邀请者输入 `/icode XXXX-XXXX` 或 `/invc XXXX-XXXX`
   - 双方获得配置的奖励

3. **管理员使用**：
   - 输入 `/inv` 查看邀请统计
   - 编辑 `plugins/InviteReward/config.yml` 自定义奖励和设置

## 数据存储

插件数据存储在 `plugins/InviteReward/data.yml` 中，包括：
- 邀请码与玩家的绑定关系
- 邀请次数统计
- 已使用邀请码的玩家记录
- 待处理的奖励队列

## 版本信息

- **插件版本**：1.0.0
- **Minecraft 版本**：1.21.8
- **API 版本**：1.16

## 注意事项

1. 奖励指令通过控制台执行，请确保指令语法正确
2. 如果使用 `give` 指令，玩家必须在线才能收到物品
3. 邀请码格式固定为 `XXXX-XXXX`（大写字母）
4. 重新生成邀请码会使旧邀请码失效
5. 定期清理过期的待处理奖励（默认24小时）

## 问题反馈

如遇到问题，请检查：
1. 控制台是否有错误日志
2. 权限配置是否正确
3. 奖励指令是否有效
4. 玩家是否满足防滥用条件

如需更多帮助，请联系插件开发者。