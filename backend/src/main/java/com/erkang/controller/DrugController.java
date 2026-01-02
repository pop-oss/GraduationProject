package com.erkang.controller;

import com.erkang.common.Result;
import com.erkang.security.RequireRole;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 药品控制器
 * _Requirements: 6.8_
 */
@RestController
@RequestMapping("/api/drugs")
@RequiredArgsConstructor
public class DrugController {

    /**
     * 获取药品列表
     */
    @GetMapping
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<List<DrugDTO>> list(@RequestParam(required = false) String keyword) {
        // 返回模拟药品数据（实际项目应从数据库查询）
        List<DrugDTO> drugs = getMockDrugs();
        
        if (keyword != null && !keyword.isEmpty()) {
            drugs = drugs.stream()
                .filter(d -> d.getName().contains(keyword))
                .toList();
        }
        
        return Result.success(drugs);
    }

    /**
     * 获取药品详情
     */
    @GetMapping("/{id}")
    @RequireRole({"DOCTOR_PRIMARY", "DOCTOR_EXPERT", "PHARMACIST"})
    public Result<DrugDTO> getById(@PathVariable Long id) {
        return getMockDrugs().stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .map(Result::success)
            .orElse(Result.fail(404, "药品不存在"));
    }

    private List<DrugDTO> getMockDrugs() {
        return Arrays.asList(
            new DrugDTO(1L, "阿莫西林胶囊", "0.5g*24粒", "盒"),
            new DrugDTO(2L, "布洛芬缓释胶囊", "0.3g*20粒", "盒"),
            new DrugDTO(3L, "氯雷他定片", "10mg*6片", "盒"),
            new DrugDTO(4L, "盐酸氨溴索口服液", "100ml", "瓶"),
            new DrugDTO(5L, "头孢克肟分散片", "0.1g*10片", "盒"),
            new DrugDTO(6L, "氧氟沙星滴耳液", "5ml", "瓶"),
            new DrugDTO(7L, "红霉素眼膏", "2g", "支"),
            new DrugDTO(8L, "复方甘草片", "100片", "瓶"),
            new DrugDTO(9L, "维生素C片", "100mg*100片", "瓶"),
            new DrugDTO(10L, "双氯芬酸钠缓释片", "75mg*10片", "盒")
        );
    }

    @Data
    public static class DrugDTO {
        private Long id;
        private String name;
        private String specification;
        private String unit;

        public DrugDTO(Long id, String name, String specification, String unit) {
            this.id = id;
            this.name = name;
            this.specification = specification;
            this.unit = unit;
        }
    }
}
