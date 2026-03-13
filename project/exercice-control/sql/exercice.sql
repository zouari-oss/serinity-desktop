-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mar. 24 fév. 2026 à 10:44
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `serinity`
--

-- --------------------------------------------------------

--
-- Structure de la table `exercise`
--

CREATE TABLE `exercise` (
  `id` int(11) NOT NULL,
  `title` varchar(120) NOT NULL,
  `type` varchar(30) NOT NULL,
  `level` int(11) NOT NULL,
  `duration_minutes` int(11) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `exercise`
--

INSERT INTO `exercise` (`id`, `title`, `type`, `level`, `duration_minutes`, `description`) VALUES
(1, 'Respiration guidée', 'respiration', 1, 10, 'Exercice simple pour se détendre'),
(9, ' L’exercice de la cohérence cardiaque', 'méditation', 2, 5, 'Je ferme un poing (droit ou gauche, cela n’a aucune importance) et je commence par compter à l’inspiration : “1, 2, 3, 4, 5”. Puis j’expire sur “1, 2, 3, 4, 5”. Ensuite, pour ma deuxième inspiration je commence par dire “2”, ainsi je sais que j’en suis… à ma deuxième inspiration ! Donc : “2, 2, 3, 4, 5” et j’expire en disant (dans ma tête toujours) : “2, 2, 3, 4, 5”. Pour la troisième inspiration, je commence… par quoi selon vous ? Bingo ! J’inspire sur : “3, 2, 3, 4, 5” et j’expire sur “3, 2, 3, 4, 5”. Le fait de se concentrer en comptant permet d’être pleinement dans la respiration (et de lâcher le mental petit à petit).\n\nJe continue ainsi jusqu’à 6, et cela me permet de savoir que j’ai respiré pendant 1 minute. Bah oui : 5 secondes d’inspiration et 5 secondes d’expiration font 10 secondes.'),
(10, 'respiration abdominale', 'respiration', 1, 8, ' il suffit de poser vos mains sur votre ventre et inspirer profondément par le nez, en le faisant gonfler comme s’il s’agissait d’un ballon. Bloquez votre respiration quelques secondes et expirez par la bouche en rentrant progressivement votre ventre jusqu’à le vider complètement. Pour plus d’efficacité, répétez mentalement « je me calme, je me relâche…» tout au long de l\'exercice.'),
(12, 'ghghff', 'respiration', 3, 6, 'chgfhgddd');

-- --------------------------------------------------------

--
-- Structure de la table `exercise_session`
--

CREATE TABLE `exercise_session` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `exercise_id` int(11) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'CREATED',
  `started_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  `active_seconds` int(11) NOT NULL DEFAULT 0,
  `last_resumed_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `exercise_session`
--

INSERT INTO `exercise_session` (`id`, `user_id`, `exercise_id`, `status`, `started_at`, `completed_at`, `feedback`, `active_seconds`, `last_resumed_at`) VALUES
(5, 1, 9, 'COMPLETED', '2026-02-17 13:02:18', '2026-02-17 13:02:33', '', 0, NULL),
(6, 1, 1, 'COMPLETED', '2026-02-17 16:15:32', '2026-02-17 16:15:35', '', 0, NULL),
(7, 1, 1, 'COMPLETED', '2026-02-17 16:21:00', '2026-02-17 16:21:04', '', 0, NULL),
(8, 1, 12, 'IN_PROGRESS', '2026-02-22 20:23:57', NULL, NULL, 0, '2026-02-22 20:23:57'),
(9, 1, 12, 'COMPLETED', '2026-02-22 20:28:24', '2026-02-22 20:28:37', '', 13, NULL),
(10, 1, 12, 'ABORTED', '2026-02-22 20:28:50', '2026-02-22 20:29:11', NULL, 18, NULL),
(11, 1, 9, 'PAUSED', '2026-02-22 20:29:42', NULL, NULL, 11, NULL),
(12, 1, 9, 'PAUSED', '2026-02-23 20:47:30', NULL, NULL, 227, NULL),
(13, 1, 10, 'ABORTED', '2026-02-23 21:20:54', '2026-02-23 21:22:23', NULL, 89, NULL),
(14, 1, 9, 'IN_PROGRESS', '2026-02-23 21:22:37', NULL, NULL, 0, '2026-02-23 21:22:37'),
(15, 1, 10, 'IN_PROGRESS', '2026-02-23 21:41:32', NULL, NULL, 0, '2026-02-23 21:41:32'),
(16, 1, 10, 'IN_PROGRESS', '2026-02-23 21:52:48', NULL, NULL, 0, '2026-02-23 21:52:48'),
(17, 1, 12, 'IN_PROGRESS', '2026-02-24 00:30:25', NULL, NULL, 0, '2026-02-24 00:30:25'),
(18, 1, 1, 'COMPLETED', '2026-02-24 00:56:55', '2026-02-24 00:57:06', '', 11, NULL),
(19, 1, 1, 'IN_PROGRESS', '2026-02-24 00:59:49', NULL, NULL, 0, '2026-02-24 00:59:49'),
(20, 1, 1, 'PAUSED', '2026-02-24 01:04:16', NULL, NULL, 25, NULL),
(21, 1, 12, 'PAUSED', '2026-02-24 01:36:30', NULL, NULL, 5, NULL),
(22, 1, 12, 'COMPLETED', '2026-02-24 07:03:25', '2026-02-24 07:04:17', '', 52, NULL),
(23, 1, 12, 'IN_PROGRESS', '2026-02-24 07:04:34', NULL, NULL, 0, '2026-02-24 07:04:34'),
(24, 1, 10, 'COMPLETED', '2026-02-24 07:23:56', '2026-02-24 07:26:22', '', 106, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `favorite`
--

CREATE TABLE `favorite` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `favorite_type` varchar(20) NOT NULL,
  `item_id` int(11) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `favorite`
--

INSERT INTO `favorite` (`id`, `user_id`, `favorite_type`, `item_id`, `created_at`) VALUES
(1, 1, 'EXERCISE', 2, '2026-02-09 20:34:26'),
(3, 1, 'EXERCISE', 3, '2026-02-09 20:36:37'),
(5, 1, 'EXERCISE', 4, '2026-02-10 16:17:37'),
(7, 1, 'EXERCISE', 5, '2026-02-14 20:42:19');

-- --------------------------------------------------------

--
-- Structure de la table `resource`
--

CREATE TABLE `resource` (
  `id` int(11) NOT NULL,
  `title` varchar(120) NOT NULL,
  `media_type` varchar(20) NOT NULL,
  `url` text DEFAULT NULL,
  `content` text DEFAULT NULL,
  `duration_seconds` int(11) DEFAULT NULL,
  `exercise_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `resource`
--

INSERT INTO `resource` (`id`, `title`, `media_type`, `url`, `content`, `duration_seconds`, `exercise_id`) VALUES
(5, 'les franjynes', 'TEXTE', 'https://lesfranjynes.com', '', 5, 9),
(7, 'the raserena', 'TEXTE', 'https://www.theraserena.com/', '', 2, 10),
(8, 'conseilsport.decathlon', 'TEXTE', 'https://conseilsport.decathlon', '', 120, 11);

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `exercise`
--
ALTER TABLE `exercise`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `exercise_session`
--
ALTER TABLE `exercise_session`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_session_exercise` (`exercise_id`);

--
-- Index pour la table `favorite`
--
ALTER TABLE `favorite`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `resource`
--
ALTER TABLE `resource`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_resource_exercise` (`exercise_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `exercise`
--
ALTER TABLE `exercise`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT pour la table `exercise_session`
--
ALTER TABLE `exercise_session`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT pour la table `favorite`
--
ALTER TABLE `favorite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `resource`
--
ALTER TABLE `resource`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
