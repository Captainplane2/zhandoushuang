package com.esports.zds;

import com.esports.zds.entity.MatchRoom;
import com.esports.zds.entity.Team;
import com.esports.zds.repository.MatchRoomRepository;
import com.esports.zds.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据迁移类，用于将现有数据标记为CS2板块
 * 注意：该类仅执行一次数据迁移，执行后可注释或删除
 */
//@Component
public class DataMigration implements CommandLineRunner {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRoomRepository matchRoomRepository;

    @Override
    public void run(String... args) throws Exception {
        // 迁移战队数据
        migrateTeams();
        // 迁移约战数据
        migrateMatchRooms();
    }

    private void migrateTeams() {
        List<Team> teams = teamRepository.findAll();
        for (Team team : teams) {
            if (team.getGameProject() == null || team.getGameProject().isEmpty()) {
                team.setGameProject("CS2");
                teamRepository.save(team);
                System.out.println("迁移战队: " + team.getName() + " 到 CS2 板块");
            }
        }
        System.out.println("战队数据迁移完成");
    }

    private void migrateMatchRooms() {
        List<MatchRoom> rooms = matchRoomRepository.findAll();
        for (MatchRoom room : rooms) {
            if (room.getGameProject() == null || room.getGameProject().isEmpty()) {
                room.setGameProject("CS2");
                matchRoomRepository.save(room);
                System.out.println("迁移约战: " + room.getTitle() + " 到 CS2 板块");
            }
        }
        System.out.println("约战数据迁移完成");
    }
}