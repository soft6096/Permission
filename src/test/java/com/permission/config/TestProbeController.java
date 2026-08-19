package com.permission.config;

import com.permission.common.web.R;
import com.permission.common.web.RequiresPermission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试探针接口：模拟一个带权限编码的受保护业务接口，供权限拦截契约测试验证 403 判定。
 */
@RestController
public class TestProbeController {

    /**
     * 探针接口：需 sys:probe:query 权限。
     *
     * @return 成功响应
     */
    @GetMapping("/sys/probe-perm")
    @RequiresPermission("sys:probe:query")
    public R<Void> probe() {
        return R.success(null);
    }
}
