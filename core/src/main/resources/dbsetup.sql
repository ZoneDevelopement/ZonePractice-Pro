CREATE TABLE IF NOT EXISTS `global_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `uuid` varchar(100) NOT NULL,
  `firstJoin` bigint NOT NULL,
  `lastJoin` bigint NOT NULL,
  `unrankedWins` int DEFAULT 0,
  `unrankedLosses` int DEFAULT 0,
  `rankedWins` int DEFAULT 0,
  `rankedLosses` int DEFAULT 0,
  `globalElo` int DEFAULT 0,
  `globalRank` varchar(100) DEFAULT NULL,
  `experience` int DEFAULT 0,
  `winStreak` int DEFAULT 0,
  `bestWinStreak` int DEFAULT 0,
  `loseStreak` int DEFAULT 0,
  `bestLoseStreak` int DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_global_stats_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `ladder_stats` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `uuid` varchar(100) NOT NULL,
  `ladder` varchar(100) NOT NULL,
  `unrankedWins` int DEFAULT 0,
  `unrankedLosses` int DEFAULT 0,
  `rankedWins` int DEFAULT 0,
  `rankedLosses` int DEFAULT 0,
  `elo` int DEFAULT 0,
  `rank` varchar(100) DEFAULT NULL,
  `kills` int DEFAULT 0,
  `deaths` int DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ladder_stats_uuid_ladder` (`uuid`, `ladder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `global_stats` ADD COLUMN IF NOT EXISTS `experience` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN IF NOT EXISTS `winStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN IF NOT EXISTS `bestWinStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN IF NOT EXISTS `loseStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN IF NOT EXISTS `bestLoseStreak` int DEFAULT 0;

ALTER TABLE `ladder_stats` ADD COLUMN IF NOT EXISTS `kills` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN IF NOT EXISTS `deaths` int DEFAULT 0;

CREATE UNIQUE INDEX IF NOT EXISTS `uk_global_stats_uuid` ON `global_stats` (`uuid`);
CREATE UNIQUE INDEX IF NOT EXISTS `uk_ladder_stats_uuid_ladder` ON `ladder_stats` (`uuid`, `ladder`);
