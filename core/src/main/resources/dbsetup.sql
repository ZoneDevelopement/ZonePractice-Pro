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
  `unrankedWinStreak` int DEFAULT 0,
  `unrankedBestWinStreak` int DEFAULT 0,
  `unrankedLoseStreak` int DEFAULT 0,
  `unrankedBestLoseStreak` int DEFAULT 0,
  `rankedWins` int DEFAULT 0,
  `rankedLosses` int DEFAULT 0,
  `rankedWinStreak` int DEFAULT 0,
  `rankedBestWinStreak` int DEFAULT 0,
  `rankedLoseStreak` int DEFAULT 0,
  `rankedBestLoseStreak` int DEFAULT 0,
  `elo` int DEFAULT 0,
  `rank` varchar(100) DEFAULT NULL,
  `kills` int DEFAULT 0,
  `deaths` int DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ladder_stats_uuid_ladder` (`uuid`, `ladder`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `global_stats` ADD COLUMN `experience` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN `winStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN `bestWinStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN `loseStreak` int DEFAULT 0;
ALTER TABLE `global_stats` ADD COLUMN `bestLoseStreak` int DEFAULT 0;

ALTER TABLE `ladder_stats` ADD COLUMN `kills` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `deaths` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `unrankedWinStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `unrankedBestWinStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `unrankedLoseStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `unrankedBestLoseStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `rankedWinStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `rankedBestWinStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `rankedLoseStreak` int DEFAULT 0;
ALTER TABLE `ladder_stats` ADD COLUMN `rankedBestLoseStreak` int DEFAULT 0;

CREATE UNIQUE INDEX `uk_global_stats_uuid` ON `global_stats` (`uuid`);
CREATE UNIQUE INDEX `uk_ladder_stats_uuid_ladder` ON `ladder_stats` (`uuid`, `ladder`);
