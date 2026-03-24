-- 3.0.34 addon 个性化设置友好化与补齐

INSERT INTO `sq_config` (`config_name`, `config_value`, `config_key`, `config_type`, `config_show`, `config_remark`, `config_null_check`, `config_disabled`)
SELECT '【个性化】01 路径非法字符替换开关', 'false', 'addon.path.slash.replace.enable', 'boolean', 1, '开启后将路径中的 / \\ : * ? " < > | 替换为自定义字符；关闭时保持原版逻辑', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sq_config` WHERE `config_key` = 'addon.path.slash.replace.enable');

INSERT INTO `sq_config` (`config_name`, `config_value`, `config_key`, `config_type`, `config_show`, `config_remark`, `config_null_check`, `config_disabled`)
SELECT '【个性化】02 路径替换字符', '-', 'addon.path.slash.replace.char', 'input', 1, '例如 + 或 x；仅在“路径非法字符替换开关”开启时生效', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sq_config` WHERE `config_key` = 'addon.path.slash.replace.char');

INSERT INTO `sq_config` (`config_name`, `config_value`, `config_key`, `config_type`, `config_show`, `config_remark`, `config_null_check`, `config_disabled`)
SELECT '【个性化】03 专辑按专辑艺术家归档', 'false', 'addon.album.artist.folder.enable', 'boolean', 1, '开启后同专辑合唱歌曲优先归入专辑艺术家目录；关闭时保持原版逻辑', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sq_config` WHERE `config_key` = 'addon.album.artist.folder.enable');

INSERT INTO `sq_config` (`config_name`, `config_value`, `config_key`, `config_type`, `config_show`, `config_remark`, `config_null_check`, `config_disabled`)
SELECT '【个性化】04 写入音轨序号到Metadata', 'false', 'addon.track.number.enable', 'boolean', 1, '仅写入标签 Track 字段，不修改文件名', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sq_config` WHERE `config_key` = 'addon.track.number.enable');

UPDATE `sq_config`
SET
    `config_name` = '【个性化】01 路径非法字符替换开关',
    `config_type` = 'boolean',
    `config_show` = 1,
    `config_remark` = '开启后将路径中的 / \\ : * ? " < > | 替换为自定义字符；关闭时保持原版逻辑',
    `config_null_check` = 1,
    `config_disabled` = 0
WHERE `config_key` = 'addon.path.slash.replace.enable';

UPDATE `sq_config`
SET
    `config_name` = '【个性化】02 路径替换字符',
    `config_type` = 'input',
    `config_show` = 1,
    `config_remark` = '例如 + 或 x；仅在“路径非法字符替换开关”开启时生效',
    `config_null_check` = 1,
    `config_disabled` = 0
WHERE `config_key` = 'addon.path.slash.replace.char';

UPDATE `sq_config`
SET
    `config_name` = '【个性化】03 专辑按专辑艺术家归档',
    `config_type` = 'boolean',
    `config_show` = 1,
    `config_remark` = '开启后同专辑合唱歌曲优先归入专辑艺术家目录；关闭时保持原版逻辑',
    `config_null_check` = 1,
    `config_disabled` = 0
WHERE `config_key` = 'addon.album.artist.folder.enable';

UPDATE `sq_config`
SET
    `config_name` = '【个性化】04 写入音轨序号到Metadata',
    `config_type` = 'boolean',
    `config_show` = 1,
    `config_remark` = '仅写入标签 Track 字段，不修改文件名',
    `config_null_check` = 1,
    `config_disabled` = 0
WHERE `config_key` = 'addon.track.number.enable';
