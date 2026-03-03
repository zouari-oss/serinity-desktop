-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3307
-- Généré le : mar. 24 fév. 2026 à 09:30
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
-- Base de données : `serinity_sleep`
--

-- --------------------------------------------------------

--
-- Structure de la table `reves`
--

CREATE TABLE `reves` (
  `id` int(11) NOT NULL,
  `sommeil_id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` text NOT NULL,
  `humeur` varchar(50) DEFAULT NULL,
  `type_reve` varchar(50) DEFAULT NULL,
  `intensite` int(11) DEFAULT NULL CHECK (`intensite` between 1 and 10),
  `couleur` tinyint(1) DEFAULT 1,
  `emotions` varchar(200) DEFAULT NULL,
  `symboles` text DEFAULT NULL,
  `recurrent` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reves`
--

INSERT INTO `reves` (`id`, `sommeil_id`, `titre`, `description`, `humeur`, `type_reve`, `intensite`, `couleur`, `emotions`, `symboles`, `recurrent`, `created_at`, `updated_at`) VALUES
(5, 6, 'maison', 'enfant', '😄 Joyeux', '💭 Normal', 5, 1, 'heureuse', 'famille', 0, '2026-02-17 15:07:52', '2026-02-23 20:10:14'),
(6, 6, 'voiture', 'voiture , accident , blood', '😰 Anxieux', '😱 Cauchemar', 8, 0, 'PEUR', NULL, 1, '2026-02-23 20:08:07', '2026-02-23 20:12:21'),
(7, 12, 'accident', 'voiture accident grave', '😢 Triste', '😱 Cauchemar', 7, 1, 'peur', NULL, 0, '2026-02-24 00:22:01', '2026-02-24 00:22:01'),
(8, 12, 'rien', 'rien rien', '😄 Joyeux', '😱 Cauchemar', 7, 1, 'rien', 'rien', 0, '2026-02-24 00:45:12', '2026-02-24 00:45:12');

-- --------------------------------------------------------

--
-- Structure de la table `sommeil`
--

CREATE TABLE `sommeil` (
  `id` int(11) NOT NULL,
  `date_nuit` date NOT NULL,
  `heure_coucher` time NOT NULL,
  `heure_reveil` time NOT NULL,
  `qualite` varchar(50) NOT NULL,
  `commentaire` text DEFAULT NULL,
  `duree_sommeil` decimal(4,2) DEFAULT NULL,
  `interruptions` int(11) DEFAULT 0,
  `humeur_reveil` varchar(50) DEFAULT NULL,
  `environnement` varchar(100) DEFAULT NULL,
  `temperature` decimal(4,1) DEFAULT NULL,
  `bruit_niveau` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sommeil`
--

INSERT INTO `sommeil` (`id`, `date_nuit`, `heure_coucher`, `heure_reveil`, `qualite`, `commentaire`, `duree_sommeil`, `interruptions`, `humeur_reveil`, `environnement`, `temperature`, `bruit_niveau`, `created_at`, `updated_at`) VALUES
(6, '2026-02-17', '07:00:00', '22:00:00', 'Moyenne', '', 0.00, 0, 'Reposé', 'Normal', 20.0, 'Léger', '2026-02-17 12:18:57', '2026-02-24 00:07:52'),
(11, '2026-02-23', '22:30:00', '07:00:00', 'Moyenne', '', 8.50, 0, '⚡ Énergisé', '🌿 Calme', 20.0, '🔉 Léger', '2026-02-23 23:47:57', '2026-02-24 00:29:53'),
(12, '2026-02-23', '22:00:00', '08:00:00', 'Excellente', '', 10.00, 0, '😌 Reposé', '🌿 Calme', 20.0, '🔉 Léger', '2026-02-23 23:48:31', '2026-02-24 00:30:07'),
(14, '2026-02-23', '23:00:00', '06:00:00', 'Bonne', '', 7.00, 0, '😐 Neutre', '🏠 Normal', 20.0, '🔇 Silencieux', '2026-02-23 23:59:15', '2026-02-24 00:30:20'),
(16, '2026-02-23', '04:15:00', '08:00:00', 'Bonne', '', 3.75, 0, '😌 Reposé', '😊 Confortable', 20.0, '🔉 Modéré', '2026-02-24 00:11:04', '2026-02-24 00:30:33'),
(17, '2026-02-23', '22:00:00', '07:00:00', 'Moyenne', '', 9.00, 0, '😐 Neutre', '😊 Confortable', 20.0, '🔉 Modéré', '2026-02-24 00:18:19', '2026-02-24 00:30:46'),
(18, '2026-02-23', '22:00:00', '07:00:00', 'Bonne', '', 9.00, 0, '😴 Fatigué', '🏠 Normal', 20.0, '🔉 Léger', '2026-02-24 00:29:01', '2026-02-24 00:29:01');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `reves`
--
ALTER TABLE `reves`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_reves_sommeil` (`sommeil_id`),
  ADD KEY `idx_reves_type` (`type_reve`);

--
-- Index pour la table `sommeil`
--
ALTER TABLE `sommeil`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_sommeil_date` (`date_nuit`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `reves`
--
ALTER TABLE `reves`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT pour la table `sommeil`
--
ALTER TABLE `sommeil`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `reves`
--
ALTER TABLE `reves`
  ADD CONSTRAINT `reves_ibfk_1` FOREIGN KEY (`sommeil_id`) REFERENCES `sommeil` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
