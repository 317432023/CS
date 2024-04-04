package com.laolang.cs.open;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cmpt.sys.comm.Constants;
import com.cmpt.sys.model.entity.SysConfig;
import com.cmpt.sys.service.SysConfigService;
import com.comm.pojo.R;
import com.frm.redis.ModeDict;
import com.frm.redis.RedisTool;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("api/open")
public class OpenApiController {

    private SysConfigService sysConfigService;
    private RedisTool redisTool;

    /**
     * 取上传地址
     * @return
     */
    @GetMapping("getUploadUrl")
    public R<String> getUploadUrl() {
        final String uploadConfigKey = "STATIC_UPLOAD";

        String uploadUrl = redisTool.hget(Constants.SYS_CONFIG_KEY, uploadConfigKey, ModeDict.APP_GROUP, 1);
        if(uploadUrl == null) {
            SysConfig sysConfig = sysConfigService.getOne(Wrappers.<SysConfig>lambdaQuery()
                    .eq(SysConfig::getConfigKey, uploadConfigKey)
            );
            uploadUrl = sysConfig == null?"":sysConfig.getConfigValue();
            if (uploadUrl == null) {
                uploadUrl = "";
            } else {
                uploadUrl = uploadUrl.trim();
            }
            redisTool.hset(Constants.SYS_CONFIG_KEY, uploadConfigKey, uploadUrl, ModeDict.APP_GROUP, 1);
        }

        return R.success(uploadUrl);
    }

}
