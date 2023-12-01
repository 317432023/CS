
SELECT 1 FROM DUAL;

create table tb_chat_user(
  `id` int unsigned not null auto_increment,
  `tenant_id` varchar(32) not null comment '租户ID',
  `nick_name` varchar(32) not null comment '昵称',
  `user_type` int unsigned default 0 not null comment '用户类型0-客人; 1-客服人员(有关联系统用户)',
  `avatar` varchar(200) comment '头像',
  `rel_id` bigint unsigned not null default 0 comment '关联用户ID',
  `create_time` datetime,
  unique index uq_index(`tenant_id`,`user_type`,`nick_name`),
  primary key(`id`)
) comment '聊天用户';

/**
 * message 两种格式分别为文本和图片: {"type":"text","content":"你好"} 例如：{"type":"image","content":"http://res.cs.com/upload/2023/09/26/01.jpeg"}
 */
create table tb_chat_message(
  `id` bigint unsigned not null auto_increment,
  `sender` int unsigned not null comment '发送者',
  `receiver` int unsigned not null comment '接收者', -- 注意接收者是一个群ID
  `message` text comment '聊天内容',
  `create_time` datetime,
  primary key(`id`)
) comment '聊天记录';

create table tb_chat_message_read(
  `id` bigint unsigned not null auto_increment,
  `msg_id` bigint unsigned not null comment '消息记录ID',
  `rcpt_id` int unsigned not null comment '接收者ID',
  `read` tinyint unsigned not null default 0 comment '是否已读',
  `update_time` datetime,
  primary key(id)
) comment '已读情况';
