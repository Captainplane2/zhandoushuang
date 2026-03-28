package com.esports.zds.controller;

import com.esports.zds.common.Result;
import com.esports.zds.entity.GameProject;
import com.esports.zds.service.GameProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 游戏板块控制器
 */
@RestController
@RequestMapping("/api/game-project")
public class GameProjectController {

    @Autowired
    private GameProjectService gameProjectService;

    /**
     * 获取所有游戏板块
     */
    @GetMapping("/list")
    public Result<List<GameProject>> list() {
        return Result.success(gameProjectService.listAll());
    }

    /**
     * 获取启用的游戏板块
     */
    @GetMapping("/list-enabled")
    public Result<List<GameProject>> listEnabled() {
        return Result.success(gameProjectService.listEnabled());
    }

    /**
     * 根据ID获取游戏板块
     */
    @GetMapping("/{id}")
    public Result<GameProject> getById(@PathVariable Long id) {
        return Result.success(gameProjectService.getById(id));
    }

    /**
     * 创建游戏板块
     */
    @PostMapping("/create")
    public Result<GameProject> create(@RequestBody GameProject gameProject) {
        return Result.success("游戏板块创建成功", gameProjectService.create(gameProject));
    }

    /**
     * 更新游戏板块
     */
    @PostMapping("/update")
    public Result<GameProject> update(@RequestBody GameProject gameProject) {
        return Result.success("游戏板块更新成功", gameProjectService.update(gameProject));
    }

    /**
     * 删除游戏板块
     */
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        gameProjectService.delete(id);
        return Result.success("游戏板块删除成功");
    }
}
