
CREATE DATABASE IF NOT EXISTS `learnenglish` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `learnenglish`;


CREATE TABLE IF NOT EXISTS `collections` (
  `id` smallint(5) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL
  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `categories` (
  `id` mediumint(9) UNSIGNED NOT NULL AUTO_INCREMENT,
  `collection_id` smallint(5) UNSIGNED NULL,
  `icon` varchar(25) NOT NULL,
  `name` varchar(50) NOT NULL,
  `name_cs` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`collection_id`) REFERENCES collections(id),
  UNIQUE KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `words` (
  `id` mediumint(9) UNSIGNED NOT NULL AUTO_INCREMENT,
  `collection_id` smallint(5) UNSIGNED NULL,
  `text` varchar(50) NOT NULL,
  `pronunciation` varchar(200) NOT NULL,
  `state` varchar(20) NOT NULL DEFAULT 'IMPORT',
  `rank` mediumint(9) UNSIGNED NOT NULL,
  `sense` JSON NOT NULL,
  `examples` JSON NOT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`collection_id`) REFERENCES collections(id),
  UNIQUE KEY (`text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `categories_words` (
  `word_id` mediumint(9) UNSIGNED NOT NULL,
  `category_id` mediumint(9) UNSIGNED NOT NULL,
  FOREIGN KEY (`word_id`) REFERENCES words(id),
  FOREIGN KEY (`category_id`) REFERENCES categories(id),
  UNIQUE KEY (`word_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `activities` (
  `uuid` varchar(255) NOT NULL,
  `timestamp` timestamp NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
