
CREATE TABLE IF NOT EXISTS  `t_money_investor_lasttradeorder` (
  `investorOid` varchar(255) NOT NULL,
  `orderTime` timestamp NULL DEFAULT NULL,
  `productOid` varchar(255) DEFAULT NULL,
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`investorOid`),
  UNIQUE KEY `idx_inverstorOid` (`investorOid`) USING BTREE,
  KEY `idx_ordertime` (`orderTime`) USING BTREE,
  KEY `idx_productOid` (`productOid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

