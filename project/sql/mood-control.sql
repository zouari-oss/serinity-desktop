-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 21, 2026 at 10:14 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `serinity`
--

-- --------------------------------------------------------

--
-- Table structure for table `emotion`
--

CREATE TABLE `emotion` (
  `id` int(11) NOT NULL,
  `name` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `emotion`
--

INSERT INTO `emotion` (`id`, `name`) VALUES
(19, 'Afraid'),
(25, 'Angry'),
(16, 'Anxious'),
(27, 'Ashamed'),
(13, 'Bored'),
(1, 'Calm'),
(7, 'Confident'),
(2, 'Content'),
(23, 'Disappointed'),
(4, 'Excited'),
(24, 'Frustrated'),
(5, 'Grateful'),
(26, 'Guilty'),
(3, 'Happy'),
(6, 'Hopeful'),
(30, 'Hurt'),
(20, 'Insecure'),
(9, 'Inspired'),
(29, 'Irritated'),
(28, 'Jealous'),
(22, 'Lonely'),
(10, 'Motivated'),
(11, 'Neutral'),
(14, 'Numb'),
(15, 'Overwhelmed'),
(8, 'Proud'),
(21, 'Sad'),
(17, 'Stressed'),
(12, 'Tired'),
(18, 'Worried');

-- --------------------------------------------------------

--
-- Table structure for table `influence`
--

CREATE TABLE `influence` (
  `id` int(11) NOT NULL,
  `name` varchar(60) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `influence`
--

INSERT INTO `influence` (`id`, `name`) VALUES
(19, 'Achievement'),
(27, 'Caffeine'),
(18, 'Conflict'),
(3, 'Deadlines'),
(11, 'Exercise'),
(20, 'Failure'),
(6, 'Family'),
(12, 'Food'),
(5, 'Friends'),
(23, 'Gaming'),
(9, 'Health'),
(17, 'Loneliness'),
(26, 'Medication'),
(15, 'Money'),
(22, 'Music'),
(14, 'News'),
(10, 'Pain'),
(7, 'Relationship'),
(21, 'Relaxation'),
(2, 'School/Work'),
(1, 'Sleep'),
(8, 'Social media'),
(4, 'Stress'),
(24, 'Study'),
(25, 'Therapy'),
(16, 'Travel/Commute'),
(13, 'Weather');

-- --------------------------------------------------------

--
-- Table structure for table `journal_entry`
--

CREATE TABLE `journal_entry` (
  `id` bigint(20) NOT NULL,
  `user_id` char(36) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `journal_entry`
--

INSERT INTO `journal_entry` (`id`, `user_id`, `title`, `content`, `created_at`, `updated_at`) VALUES
(3, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 'Chillin', 'Q1[journal.prompt.context]\nA1: yerrr\n\nQ2[journal.prompt.inner]\nA2: wahanenin\n\nQ3[journal.prompt.meaning]\nA3: fasho\n', '2026-02-03 04:27:17', '2026-02-21 03:14:58'),
(4, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 'frustration !!', 'Q1[journal.prompt.context]\nA1: bellfam, projet, javaFX, come, national anthem.\n\nQ2[journal.prompt.inner]\nA2: fhemnty edheka, grimace, la vie, missing significant other , females.\n\nQ3[journal.prompt.meaning]\nA3: l\'amour ou tchichi, tablia rose bechkir bnawar brown/pink. black / white / blue women with flower or dragon tattos\n', '2026-02-09 18:03:24', '2026-02-21 03:15:10'),
(6, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 'mauvas journe', 'Q1[journal.prompt.context]\nA1: un jour tré mavais\n\nQ2[journal.prompt.inner]\nA2: colére\n\nQ3[journal.prompt.meaning]\nA3: pas de controle\n', '2026-02-16 17:37:11', '2026-02-21 03:15:21');

-- --------------------------------------------------------

--
-- Table structure for table `mood_entry`
--

CREATE TABLE `mood_entry` (
  `id` bigint(20) NOT NULL,
  `user_id` char(36) NOT NULL,
  `entry_date` datetime NOT NULL DEFAULT current_timestamp(),
  `moment_type` enum('MOMENT','DAY') NOT NULL,
  `mood_level` tinyint(4) NOT NULL,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ;

--
-- Dumping data for table `mood_entry`
--

INSERT INTO `mood_entry` (`id`, `user_id`, `entry_date`, `moment_type`, `mood_level`, `updated_at`) VALUES
(1, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 01:47:30', 'MOMENT', 5, '2026-02-21 04:55:38'),
(2, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 01:47:32', 'MOMENT', 5, '2026-02-21 04:55:47'),
(3, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 01:53:40', 'DAY', 2, '2026-02-21 04:59:49'),
(4, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 03:12:03', 'MOMENT', 5, '2026-02-21 04:59:54'),
(5, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 03:55:11', 'MOMENT', 1, '2026-02-21 04:59:59'),
(7, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 04:13:24', 'MOMENT', 4, '2026-02-21 05:00:04'),
(8, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 04:22:56', 'DAY', 1, '2026-02-21 05:00:10'),
(9, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 04:24:31', 'DAY', 3, '2026-02-21 05:02:53'),
(10, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 05:05:46', 'MOMENT', 2, '2026-02-21 05:02:58'),
(11, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 05:09:13', 'MOMENT', 2, '2026-02-21 05:03:04'),
(15, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 07:59:15', 'MOMENT', 1, '2026-02-21 05:03:09'),
(16, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-28 13:12:23', 'DAY', 5, '2026-02-21 05:03:15'),
(17, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 20:53:39', 'DAY', 5, '2026-02-21 05:03:21'),
(18, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 20:53:41', 'DAY', 5, '2026-02-21 05:03:28'),
(19, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 21:01:43', 'MOMENT', 3, '2026-02-21 05:03:33'),
(20, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 22:28:26', 'MOMENT', 1, '2026-02-21 05:03:38'),
(21, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 22:44:22', 'MOMENT', 5, '2026-02-21 05:03:44'),
(22, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:03:17', 'DAY', 5, '2026-02-21 05:03:49'),
(23, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:05:25', 'MOMENT', 2, '2026-02-21 05:03:54'),
(24, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:07:13', 'DAY', 4, '2026-02-21 05:03:58'),
(25, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:08:20', 'MOMENT', 3, '2026-02-21 05:04:02'),
(26, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:08:57', 'MOMENT', 4, '2026-02-21 05:04:08'),
(28, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:11:08', 'MOMENT', 3, '2026-02-21 05:04:13'),
(29, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-30 23:32:20', 'DAY', 1, '2026-02-21 05:04:17'),
(30, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-01-31 01:00:10', 'MOMENT', 4, '2026-02-21 05:04:22'),
(33, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-02 02:49:42', 'MOMENT', 5, '2026-02-21 05:05:14'),
(34, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-09 18:06:31', 'DAY', 5, '2026-02-21 05:05:09'),
(35, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-09 18:22:12', 'DAY', 4, '2026-02-21 05:05:05'),
(36, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-10 00:14:43', 'MOMENT', 1, '2026-02-21 05:04:59'),
(37, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-10 23:34:21', 'MOMENT', 3, '2026-02-21 05:04:55'),
(40, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-12 01:55:45', 'MOMENT', 1, '2026-02-21 05:04:50'),
(42, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-15 20:00:17', 'MOMENT', 1, '2026-02-21 05:04:46'),
(43, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-16 19:39:27', 'MOMENT', 2, '2026-02-21 05:04:41'),
(46, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', '2026-02-17 16:27:02', 'MOMENT', 5, '2026-02-21 05:04:36');

-- --------------------------------------------------------

--
-- Table structure for table `mood_entry_emotion`
--

CREATE TABLE `mood_entry_emotion` (
  `mood_entry_id` bigint(20) NOT NULL,
  `emotion_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `mood_entry_emotion`
--

INSERT INTO `mood_entry_emotion` (`mood_entry_id`, `emotion_id`) VALUES
(1, 3),
(1, 15),
(1, 16),
(1, 19),
(2, 3),
(2, 15),
(2, 16),
(2, 19),
(3, 13),
(3, 21),
(3, 22),
(3, 24),
(3, 27),
(4, 2),
(4, 3),
(4, 4),
(4, 7),
(4, 10),
(5, 17),
(5, 23),
(5, 24),
(7, 19),
(8, 28),
(9, 26),
(10, 19),
(11, 17),
(15, 5),
(15, 7),
(15, 8),
(15, 10),
(16, 1),
(16, 2),
(16, 3),
(16, 4),
(16, 5),
(17, 1),
(17, 2),
(17, 3),
(17, 4),
(17, 5),
(18, 1),
(18, 2),
(18, 3),
(18, 4),
(18, 5),
(19, 30),
(20, 1),
(21, 1),
(21, 2),
(21, 3),
(21, 4),
(21, 5),
(22, 2),
(23, 1),
(24, 16),
(25, 1),
(26, 16),
(28, 1),
(29, 5),
(30, 1),
(30, 3),
(30, 7),
(30, 10),
(30, 12),
(33, 3),
(33, 4),
(33, 6),
(33, 7),
(33, 8),
(34, 13),
(34, 14),
(34, 22),
(34, 24),
(34, 25),
(35, 21),
(36, 9),
(36, 11),
(36, 20),
(36, 21),
(36, 24),
(37, 1),
(40, 1),
(40, 6),
(40, 17),
(40, 23),
(40, 30),
(42, 12),
(42, 14),
(42, 17),
(42, 18),
(42, 21),
(43, 8),
(43, 12),
(43, 14),
(43, 15),
(43, 23),
(46, 1),
(46, 5),
(46, 6),
(46, 8),
(46, 9);

-- --------------------------------------------------------

--
-- Table structure for table `mood_entry_influence`
--

CREATE TABLE `mood_entry_influence` (
  `mood_entry_id` bigint(20) NOT NULL,
  `influence_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `mood_entry_influence`
--

INSERT INTO `mood_entry_influence` (`mood_entry_id`, `influence_id`) VALUES
(1, 18),
(1, 19),
(1, 20),
(2, 18),
(2, 19),
(2, 20),
(3, 4),
(3, 5),
(3, 7),
(3, 18),
(3, 20),
(4, 2),
(4, 5),
(4, 11),
(4, 12),
(4, 15),
(5, 18),
(5, 19),
(5, 20),
(7, 18),
(8, 7),
(9, 24),
(10, 18),
(11, 17),
(15, 11),
(15, 12),
(15, 14),
(15, 19),
(16, 1),
(16, 2),
(16, 3),
(16, 4),
(16, 5),
(17, 1),
(17, 2),
(17, 3),
(17, 4),
(17, 5),
(18, 1),
(18, 2),
(18, 3),
(18, 4),
(18, 5),
(19, 27),
(20, 1),
(21, 2),
(21, 8),
(21, 16),
(22, 2),
(23, 1),
(24, 17),
(25, 1),
(26, 1),
(28, 1),
(29, 5),
(30, 1),
(30, 17),
(30, 18),
(30, 19),
(30, 20),
(33, 2),
(33, 5),
(33, 12),
(33, 22),
(33, 27),
(34, 1),
(34, 2),
(34, 4),
(34, 5),
(34, 17),
(35, 20),
(36, 3),
(36, 6),
(36, 14),
(36, 16),
(37, 1),
(40, 2),
(40, 3),
(40, 13),
(40, 18),
(42, 22),
(43, 1),
(43, 6),
(43, 7),
(43, 15),
(46, 2),
(46, 5),
(46, 6);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `emotion`
--
ALTER TABLE `emotion`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `influence`
--
ALTER TABLE `influence`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Indexes for table `journal_entry`
--
ALTER TABLE `journal_entry`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_journal_user_created` (`user_id`,`created_at`);

--
-- Indexes for table `mood_entry`
--
ALTER TABLE `mood_entry`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_mood_entry_user_date` (`user_id`,`entry_date`);

--
-- Indexes for table `mood_entry_emotion`
--
ALTER TABLE `mood_entry_emotion`
  ADD PRIMARY KEY (`mood_entry_id`,`emotion_id`),
  ADD KEY `emotion_id` (`emotion_id`);

--
-- Indexes for table `mood_entry_influence`
--
ALTER TABLE `mood_entry_influence`
  ADD PRIMARY KEY (`mood_entry_id`,`influence_id`),
  ADD KEY `influence_id` (`influence_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `emotion`
--
ALTER TABLE `emotion`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `influence`
--
ALTER TABLE `influence`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `journal_entry`
--
ALTER TABLE `journal_entry`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `mood_entry`
--
ALTER TABLE `mood_entry`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
