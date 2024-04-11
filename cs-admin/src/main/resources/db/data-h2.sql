
/*客服机构*/
INSERT INTO sys_org VALUES ('3', 'FT1', 'SUB', 0, 1, 0, 'FT1', 'http://127.0.0.1:9090/api/mbr/getInfo', '199828', null, '2021-02-02 01:01:59', 'admin', '2020-11-12 21:01:32', 'admin');
INSERT INTO sys_org VALUES ('4', 'FT1@测试站', 'BR', 3, 2, 0, 'FT1@6', '', '199829', null, '2021-02-02 01:01:59', 'admin', '2020-11-12 21:01:32', 'admin');

/*客服权限组*/
INSERT INTO sys_role VALUES ('7', 'MANAGER', 'FT1管理员', '应用管理员', 'SUB', '3', '0', '2021-02-08 10:00:00', '', '2021-02-08 10:00:00', '');
INSERT INTO sys_role VALUES ('8', 'STAFF', 'FT1测试站客服人员', '站点客服人员', 'BR', '4', '0', '2021-02-08 10:00:00', '', '2021-02-08 10:00:00', '');

/*增加客服菜单*/
INSERT INTO `sys_menu` VALUES ('34', '0', 'CS', '2', '/cs', null, '1', '0', '0', '1', '客服模块', 'tree', null, '1', '0', '0', null, '2021-03-21 15:24:01', '', null, '');
INSERT INTO `sys_menu` VALUES ('35', '34', 'Sessions', '0', '/cs/sessions', null, '0', '0', '0', '5', '客服会话', 'lock', null, '1', '0', '0', null, '2021-03-21 15:25:04', '', null, '');
INSERT INTO `sys_menu` VALUES ('36', '35', null, '1', null, '/cs/sessions/query/**', '0', '0', '0', '1', '查看', null, 'cs:sessions:view', '1', '0', '0', null, '2021-03-21 15:25:06', '', null, '');

INSERT INTO `sys_role_menu` VALUES(7,1),(7,34),(7,35),(7,36);
INSERT INTO `sys_role_menu` VALUES(8,1),(8,34),(8,35),(8,36);

/*添加测试用户 并指定 其角色为客服*/
INSERT INTO sys_user VALUES ('7', 'ma01', '$2a$10$r0IrohJ71YsYzqs1kK62HeUF9HpZcuK65kajt0QUuKaHldzpoDPsy', 'FT1管理01', '0', null, null, null, 0, 3, '0', '00','',null, null, '2020-11-12 21:04:23', '', null, '');
INSERT INTO sys_user VALUES ('8', 'kf01', '$2a$10$r0IrohJ71YsYzqs1kK62HeUF9HpZcuK65kajt0QUuKaHldzpoDPsy', 'FT1客服01', '0', null, null, null, 0, 4, '0', '00','',null, null, '2020-11-12 21:04:23', '', null, '');
INSERT INTO sys_user_role VALUES ('7', '7');
INSERT INTO sys_user_role VALUES ('8', '8');


/*会员信息*/
INSERT INTO mbr_user VALUES ('1', 'test', 'e10adc3949ba59abbe56e057f20f883e', '歪歪', '0', null, null, null, 0, '0', 0, '',null,'备注#', '2020-11-12 21:04:23', '', null, '');

insert into tb_chat_user(tenant_id,nick_name,user_type,avatar,rel_id,create_time) values
('FT1@6', 'nwp', 0, '/upload/faces/1.jpg', 0, '2023-10-18 21:45:59') -- 客人
;
insert into tb_chat_user(tenant_id,nick_name,user_type,avatar,rel_id,create_time) values
('FT1@6', 'FT1客服01', 1, '/upload/faces/2.jpg', 8, '2023-10-18 21:45:59') --
;

-- 测试
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 21:58:00');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 21:59:00');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:01');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:02');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:02');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:03');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:04');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:05');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:06');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:07');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:08');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:09');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:10');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:11');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:12');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:13');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:14');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:15');
insert into tb_chat_message(sender,receiver,message,create_time) values(1,1,'{"type":"text","value":"请问什么时候开放注册"}','2023-10-29 22:00:16');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"text","value":"您好，请关注网址首页公告"}','2023-10-29 22:00:17');
insert into tb_chat_message(sender,receiver,message,create_time) values(2,1,'{"type":"image","value":"/upload/jpeg/2023/11/05/676ca8e38c071ca7856a63e2eeb3efa4-e7e4c546-b462-41ae-a7cf-320d03cea94c.jpeg"}','2023-10-29 22:00:18');

update sys_config set config_value='http://be.laolang-cs.com' where config_key='STATIC_DOMAIN';
update sys_config set config_value='http://be.laolang-cs.com/upload' where config_key='STATIC_UPLOAD';


