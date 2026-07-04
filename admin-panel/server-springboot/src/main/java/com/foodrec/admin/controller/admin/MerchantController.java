package com.foodrec.admin.controller.admin;

import com.foodrec.admin.common.Result;
import com.foodrec.admin.entity.Dish;
import com.foodrec.admin.entity.Stall;
import com.foodrec.admin.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class MerchantController {

    @Autowired
    private AdminService adminService;

    // ==================== 档口管理 ====================
    @GetMapping("/stalls")
    public Result<Map<String, Object>> getStalls(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getStallList(keyword, page, pageSize),
            "total", adminService.getStallCount(keyword)
        ));
    }

    @PostMapping("/stalls")
    public Result<String> addStall(@Valid @RequestBody Stall stall) {
        return adminService.addStall(stall)
            ? Result.ok("添加成功")
            : Result.fail("添加失败");
    }

    @PutMapping("/stalls/{id}")
    public Result<String> updateStall(@PathVariable Long id, @Valid @RequestBody Stall stall) {
        stall.setStallId(id);
        return adminService.updateStall(stall)
            ? Result.ok("更新成功")
            : Result.fail("更新失败");
    }

    @DeleteMapping("/stalls/{id}")
    public Result<String> deleteStall(@PathVariable Long id) {
        return adminService.deleteStall(id)
            ? Result.ok("删除成功")
            : Result.fail("删除失败");
    }

    @GetMapping("/stalls/{id}/dishes")
    public Result<Map<String, Object>> getStallDishes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getStallDishes(id, page, pageSize)
        ));
    }

    // ==================== 菜品管理 ====================
    @GetMapping("/dishes")
    public Result<Map<String, Object>> getDishes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        return Result.ok(Map.of(
            "list", adminService.getDishList(keyword, category, page, pageSize),
            "total", adminService.getDishCount(keyword, category)
        ));
    }

    @GetMapping("/dishes/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        Dish dish = adminService.getDishById(id);
        return dish != null ? Result.ok(dish) : Result.fail("菜品不存在");
    }

    @PostMapping("/dishes")
    public Result<String> addDish(@Valid @RequestBody Dish dish) {
        return adminService.addDish(dish)
            ? Result.ok("添加成功")
            : Result.fail("添加失败");
    }

    @PutMapping("/dishes/{id}")
    public Result<String> updateDish(@PathVariable Long id, @Valid @RequestBody Dish dish) {
        dish.setDishId(id);
        return adminService.updateDish(dish)
            ? Result.ok("更新成功")
            : Result.fail("更新失败");
    }

    @DeleteMapping("/dishes/{id}")
    public Result<String> deleteDish(@PathVariable Long id) {
        return adminService.deleteDish(id)
            ? Result.ok("删除成功")
            : Result.fail("删除失败");
    }
}
