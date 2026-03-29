-- 修复约战ID 16的matchStatus字段
-- 将WAITING更新为READY，因为该约战已经被应战

UPDATE t_match_room 
SET match_status = 'READY' 
WHERE id = 16 AND status = 1;

-- 验证更新结果
SELECT id, status, match_status, title, match_time 
FROM t_match_room 
WHERE id = 16;