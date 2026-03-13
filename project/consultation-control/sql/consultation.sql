-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : mar. 03 mars 2026 à 04:59
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
-- Base de données : `thedoctor`
--

-- --------------------------------------------------------

--
-- Structure de la table `consultations`
--

CREATE TABLE `consultations` (
  `id` int(11) NOT NULL,
  `rapport_id` int(11) NOT NULL,
  `rendez_vous_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `date_consultation` datetime NOT NULL,
  `diagnostic` text DEFAULT NULL,
  `prescription` text DEFAULT NULL,
  `notes` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `consultations`
--

INSERT INTO `consultations` (`id`, `rapport_id`, `rendez_vous_id`, `doctor_id`, `date_consultation`, `diagnostic`, `prescription`, `notes`) VALUES
(1, 1, 21, 2, '2026-02-24 10:47:52', 'schizophrenia	', 'lexomil', 'need supervision'),
(2, 1, 17, 2, '2026-03-02 22:03:10', 'aaaaaaaaaa', 'aaaaaaaaaaaa', 'aaaaaaaaaaaaaaaaaa');

-- --------------------------------------------------------

--
-- Structure de la table `rapports`
--

CREATE TABLE `rapports` (
  `id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `date_creation` date NOT NULL,
  `resume_general` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rapports`
--

INSERT INTO `rapports` (`id`, `patient_id`, `date_creation`, `resume_general`) VALUES
(1, 1, '2026-02-15', 'Dossier médical du patient'),
(2, 5, '2026-02-16', 'JUnit Rapport'),
(3, 7, '2026-02-16', 'JUnit Rapport'),
(4, 9, '2026-02-16', 'JUnit Rapport'),
(5, 11, '2026-02-16', 'JUnit Rapport');

-- --------------------------------------------------------

--
-- Structure de la table `rendez_vous`
--

CREATE TABLE `rendez_vous` (
  `id` int(11) NOT NULL,
  `patient_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `motif` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `date_time` datetime NOT NULL,
  `status` enum('EN_ATTENTE','APPROUVE','REFUSE','MODIFICATION_PROPOSEE') NOT NULL DEFAULT 'EN_ATTENTE',
  `proposed_date_time` datetime DEFAULT NULL,
  `doctor_note` varchar(255) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `rendez_vous`
--

INSERT INTO `rendez_vous` (`id`, `patient_id`, `doctor_id`, `motif`, `description`, `date_time`, `status`, `proposed_date_time`, `doctor_note`, `created_at`) VALUES
(1, 1, 2, 'bla bla bla', NULL, '2026-02-09 15:00:00', 'APPROUVE', '2026-02-10 20:00:00', 'bla bla blabla bla blabla bla bla', '2026-02-09 22:09:25'),
(2, 1, 2, 'bla bla ', 'azazaz bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ', '2026-01-12 15:00:00', 'APPROUVE', NULL, NULL, '2026-02-09 22:16:32'),
(3, 1, 3, 'bla bla bla bla bla bla bla bla ', 'bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla bla ', '2026-02-12 15:00:00', 'EN_ATTENTE', NULL, NULL, '2026-02-09 22:40:08'),
(5, 1, 3, 'azazaz', '', '2026-02-15 11:55:00', 'EN_ATTENTE', NULL, NULL, '2026-02-15 13:41:28'),
(6, 1, 3, 'qsdd', 'qscqsdqds', '2026-02-18 11:11:00', 'EN_ATTENTE', NULL, NULL, '2026-02-16 12:56:42'),
(7, 1, 0, 'qdcsqdc', ' dscnk ù in lùkn ùkln ', '2026-02-18 11:55:00', 'EN_ATTENTE', NULL, NULL, '2026-02-16 12:59:16'),
(8, 5, 6, 'JUnit RDV', NULL, '2026-02-16 14:53:59', 'APPROUVE', NULL, NULL, '2026-02-16 13:53:58'),
(9, 7, 8, 'JUnit RDV', NULL, '2026-02-16 14:53:59', 'APPROUVE', NULL, NULL, '2026-02-16 13:53:58'),
(10, 9, 10, 'JUnit RDV', NULL, '2026-01-14 14:53:59', 'APPROUVE', NULL, NULL, '2026-02-16 13:53:58'),
(11, 11, 12, 'JUnit RDV', NULL, '2026-02-16 14:53:59', 'APPROUVE', NULL, NULL, '2026-02-16 13:53:58'),
(16, 1, 8, 'azaza azde zad da', 'asmaa nayek **** yta ajs nak sal n****', '2026-02-25 10:20:00', 'EN_ATTENTE', NULL, NULL, '2026-02-23 09:17:25'),
(17, 1, 3, 'xxxxxxxx xxx', 'xxxx fyxk **** **** shir baby no oh yeah', '2026-02-25 11:11:00', 'APPROUVE', NULL, NULL, '2026-02-23 09:18:43'),
(18, 1, 3, 'nnn nnn nnnn', 'nnnn **** nnn **** bbbb bb yeah', '2026-02-25 10:00:00', 'EN_ATTENTE', NULL, NULL, '2026-02-23 09:20:32'),
(19, 1, 3, ' maladie ', 'bonjour je suis **** , pour le momement ****  **** sucker', '2026-02-25 10:10:00', 'EN_ATTENTE', NULL, NULL, '2026-02-23 19:01:09'),
(20, 1, 2, 'bad feeling', 'i feel ******* bad', '2026-02-26 10:00:00', 'APPROUVE', NULL, NULL, '2026-02-24 09:39:55'),
(21, 1, 2, 'bad feeling', 'i feel so ******* bad', '2026-02-27 10:00:00', 'APPROUVE', NULL, NULL, '2026-02-24 09:46:39'),
(22, 1, 2, 'fqdqsf', 'dsqdqs', '2026-02-27 10:00:00', 'EN_ATTENTE', NULL, NULL, '2026-02-24 09:54:51'),
(23, 1, 2, 'motif', '*******', '2026-02-20 10:00:00', 'EN_ATTENTE', NULL, NULL, '2026-02-24 11:51:44');

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `role` enum('PATIENT','DOCTOR') NOT NULL,
  `speciality` varchar(150) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `full_name`, `email`, `phone`, `address`, `role`, `speciality`, `created_at`) VALUES
(1, 'Aziz Patient', 'dbwes23@gmail.com', '', NULL, 'PATIENT', NULL, '2026-02-09 23:08:30'),
(2, 'Dr Ahmed Ben Salah', 'dbwes23@gmail.com', '20000001', 'Centre Médical Lac 2, Tunis', 'DOCTOR', 'Child Psychology', '2026-02-09 23:08:30'),
(3, 'Dr Mariem Trabelsi', 'dbwes23@gmail.com', '20000002', 'Clinique Ennasr 2, Ariana', 'DOCTOR', 'Child Psychology', '2026-02-09 23:08:30'),
(4, 'Dr Youssef Gharbi', 'dbwes23@gmail.com', '20000003', 'Cabinet Dentaire Hammamet, Nabeul', 'DOCTOR', 'Clinical Psychology', '2026-02-09 23:08:30'),
(6, 'amira mansour', 'doctor@test.com', '11111111', 'Clinic Address', 'DOCTOR', 'Clinical Psychology', '2026-02-16 14:53:58'),
(8, 'ghazi fadaoui', 'doctor@test.com', '11111111', 'Clinic Address', 'DOCTOR', 'Clinical Psychology', '2026-02-16 14:53:58'),
(12, 'hmida lakdher', 'doctor@test.com', '11111111', 'Clinic Address', 'DOCTOR', 'Geriatric Psychology', '2026-02-16 14:53:58');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `consultations`
--
ALTER TABLE `consultations`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `rendez_vous_id` (`rendez_vous_id`),
  ADD KEY `rapport_id` (`rapport_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Index pour la table `rapports`
--
ALTER TABLE `rapports`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `patient_id` (`patient_id`);

--
-- Index pour la table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  ADD PRIMARY KEY (`id`),
  ADD KEY `patient_id` (`patient_id`),
  ADD KEY `doctor_id` (`doctor_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `consultations`
--
ALTER TABLE `consultations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `rapports`
--
ALTER TABLE `rapports`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT pour la table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
