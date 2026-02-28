-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Feb 28, 2026 at 07:21 PM
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
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `parent_id` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`id`, `name`, `slug`, `description`, `parent_id`) VALUES
(1, 'General', 'general', 'Talk about anything', NULL),
(2, 'Cognitive Behavioral Therapy', 'cognitive-behavioral-therapy', 'CBT techniques, workbooks, and resources for restructuring negative thought patterns', NULL),
(3, 'Mindfulness Meditation', 'mindfulness-meditation', 'Guided meditations, mindfulness exercises, and techniques for present-moment awareness', NULL),
(4, 'Trauma Recovery', 'trauma-recovery', 'Resources for healing from PTSD, childhood trauma, and complex trauma', NULL),
(5, 'Child Psychology', 'child-psychology', 'Developmental psychology resources, parenting strategies, and child mental health', NULL),
(6, 'Sleep Hygiene', 'sleep-hygiene', 'Techniques and products for improving sleep quality and treating insomnia', NULL),
(7, 'Addiction Recovery', 'addiction-recovery', 'Support resources for substance abuse, behavioral addictions, and recovery maintenance', NULL),
(8, 'Neuropsychology', 'neuropsychology', 'Brain-behavior relationships, cognitive function, and neurological basis of mental health', NULL),
(9, 'Relationship Counseling', 'relationship-counseling', 'Resources for couples therapy, communication skills, and healthy relationships', NULL),
(10, 'Positive Psychology', 'positive-psychology', 'Happiness research, strengths-based approaches, and flourishing techniques', NULL),
(11, 'Eating Disorder Support', 'eating-disorder-support', 'Resources for anorexia, bulimia, binge eating, and body image issues', NULL),
(12, 'ADHD Resources', 'adhd-resources', 'Tools and strategies for attention deficit hyperactivity disorder management', NULL),
(13, 'Grief Counseling', 'grief-counseling', 'Resources for processing loss, bereavement support, and complicated grief', NULL),
(14, 'Workplace Mental Health', 'workplace-mental-health', 'Burnout prevention, work-life balance, and mental health in professional settings', NULL),
(15, 'Anxiety Management', 'anxiety-management', 'Resources and tools for understanding and managing anxiety disorders, panic attacks, and stress', NULL),
(48, 'New Test Category', 'new-test-cat', 'Another test category', NULL),
(73, 'test', 'test', 'test category', 1),
(74, 'anxiety hghg', 'anxiety-hghg', 'tetstetstet', 3);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `thread_id` bigint(20) UNSIGNED NOT NULL,
  `type` varchar(20) NOT NULL,
  `content` varchar(200) NOT NULL,
  `seen` tinyint(1) DEFAULT 0,
  `date` date NOT NULL DEFAULT current_timestamp(),
  `user_id` char(36) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `postinteraction`
--

CREATE TABLE `postinteraction` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `thread_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` char(36) NOT NULL,
  `follow` tinyint(4) NOT NULL,
  `vote` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `postinteraction`
--

INSERT INTO `postinteraction` (`id`, `thread_id`, `user_id`, `follow`, `vote`) VALUES
(1, 1, '1', 1, 0),
(2, 1, '1', 0, 1),
(3, 1, '3', 0, -1),
(4, 1, '3', 1, 0),
(5, 1, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(8, 1, '1', 0, -1),
(11, 2, '1', 0, 1),
(12, 2, '1', 1, 0),
(13, 2, '3', 0, -1),
(14, 2, '3', 0, 1),
(15, 2, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(18, 2, '1', 1, 0),
(21, 3, '1', 1, 0),
(22, 3, '1', 0, 1),
(23, 3, '3', 1, 0),
(24, 3, '3', 0, -1),
(25, 3, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(28, 3, '1', 0, 1),
(31, 4, '1', 0, 1),
(32, 4, '1', 0, 1),
(33, 4, '3', 1, 0),
(34, 4, '3', 0, 1),
(35, 4, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(38, 4, '1', 0, 1),
(41, 5, '1', 0, 1),
(42, 5, '1', 1, 0),
(43, 5, '3', 0, -1),
(44, 5, '3', 0, 1),
(45, 5, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(48, 5, '1', 1, 0),
(51, 6, '1', 1, 0),
(52, 6, '1', 0, 1),
(53, 6, '3', 0, 1),
(54, 6, '3', 1, 0),
(55, 6, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(58, 6, '1', 0, 1),
(61, 7, '1', 0, -1),
(62, 7, '1', 1, 0),
(63, 7, '3', 0, 1),
(64, 7, '3', 0, 1),
(65, 7, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(68, 7, '1', 1, 0),
(71, 8, '1', 1, 0),
(72, 8, '1', 0, 1),
(73, 8, '3', 0, 1),
(74, 8, '3', 1, 0),
(75, 8, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(78, 8, '1', 0, 1),
(81, 9, '1', 0, 1),
(82, 9, '1', 1, 0),
(83, 9, '3', 0, 1),
(84, 9, '3', 0, -1),
(85, 9, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(88, 9, '1', 1, 0),
(91, 10, '1', 1, 0),
(92, 10, '1', 0, 1),
(93, 10, '3', 0, 1),
(94, 10, '3', 1, 0),
(95, 10, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(98, 10, '1', 0, 1),
(101, 11, '1', 0, 1),
(102, 11, '1', 0, 1),
(103, 11, '3', 1, 0),
(104, 11, '3', 0, -1),
(105, 11, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(108, 11, '1', 0, 1),
(111, 12, '1', 1, 0),
(112, 12, '1', 0, -1),
(113, 12, '3', 0, 1),
(114, 12, '3', 1, 0),
(115, 12, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(118, 12, '1', 0, 1),
(121, 13, '1', 0, 1),
(122, 13, '1', 1, 0),
(123, 13, '3', 0, 1),
(124, 13, '3', 0, 1),
(125, 13, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(128, 13, '1', 1, 0),
(131, 14, '1', 1, 0),
(132, 14, '1', 0, 1),
(133, 14, '3', 0, 1),
(134, 14, '3', 1, 0),
(135, 14, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(138, 14, '1', 0, 1),
(141, 15, '1', 0, 1),
(142, 15, '1', 1, 0),
(143, 15, '3', 0, 1),
(144, 15, '3', 0, 1),
(145, 15, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(148, 15, '1', 1, 0),
(151, 16, '1', 1, 0),
(152, 16, '1', 0, 1),
(153, 16, '3', 0, 1),
(154, 16, '3', 1, 0),
(155, 16, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(158, 16, '1', 0, 1),
(161, 17, '1', 0, 1),
(162, 17, '1', 1, 0),
(163, 17, '3', 0, -1),
(164, 17, '3', 0, 1),
(165, 17, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(168, 17, '1', 1, 0),
(171, 18, '1', 1, 1),
(172, 18, '1', 0, 1),
(173, 18, '3', 0, 1),
(174, 18, '3', 1, 0),
(175, 18, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, -1),
(178, 18, '1', 0, -1),
(181, 19, '1', 0, 1),
(182, 19, '1', 1, 0),
(183, 19, '3', 0, 1),
(184, 19, '3', 0, 1),
(185, 19, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(188, 19, '1', 1, 0),
(191, 20, '1', 1, 0),
(192, 20, '1', 0, 1),
(193, 20, '3', 0, 1),
(194, 20, '3', 1, 0),
(195, 20, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(198, 20, '1', 0, -1),
(201, 21, '1', 0, 1),
(202, 21, '1', 1, 0),
(203, 21, '3', 0, -1),
(204, 21, '3', 0, 1),
(205, 21, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(208, 21, '1', 1, 0),
(211, 22, '1', 1, 0),
(212, 22, '1', 0, 1),
(213, 22, '3', 0, 1),
(214, 22, '3', 1, 0),
(215, 22, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(218, 22, '1', 0, 1),
(221, 23, '1', 0, 1),
(222, 23, '1', 1, 0),
(223, 23, '3', 0, 1),
(224, 23, '3', 0, 1),
(225, 23, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(228, 23, '1', 1, 0),
(231, 24, '1', 1, 0),
(232, 24, '1', 0, 1),
(233, 24, '3', 0, 1),
(234, 24, '3', 1, 0),
(235, 24, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(238, 24, '1', 0, 1),
(241, 25, '1', 0, 1),
(242, 25, '1', 1, 0),
(243, 25, '3', 0, 1),
(244, 25, '3', 0, 1),
(245, 25, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(248, 25, '1', 1, 0),
(251, 26, '1', 1, 0),
(252, 26, '1', 0, 1),
(253, 26, '3', 0, 1),
(254, 26, '3', 1, 0),
(255, 26, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(258, 26, '1', 0, -1),
(261, 27, '1', 0, 1),
(262, 27, '1', 1, 0),
(263, 27, '3', 0, 1),
(264, 27, '3', 0, -1),
(265, 27, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(268, 27, '1', 1, 0),
(271, 28, '1', 1, 0),
(272, 28, '1', 0, 1),
(273, 28, '3', 0, 1),
(274, 28, '3', 1, 0),
(275, 28, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(278, 28, '1', 0, 1),
(281, 29, '1', 0, 1),
(282, 29, '1', 1, 0),
(283, 29, '3', 0, 1),
(284, 29, '3', 0, 1),
(285, 29, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(288, 29, '1', 1, 0),
(291, 30, '1', 1, 0),
(292, 30, '1', 0, 1),
(293, 30, '3', 0, 1),
(294, 30, '3', 1, 0),
(295, 30, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(298, 30, '1', 0, -1),
(301, 31, '1', 0, 1),
(302, 31, '1', 1, 0),
(303, 31, '3', 0, 1),
(304, 31, '3', 0, 1),
(305, 31, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(308, 31, '1', 1, 0),
(311, 32, '1', 1, 0),
(312, 32, '1', 0, 1),
(313, 32, '3', 0, 1),
(314, 32, '3', 1, 0),
(315, 32, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, -1),
(318, 32, '1', 0, 1),
(321, 33, '1', 0, 1),
(322, 33, '1', 1, 0),
(323, 33, '3', 0, 1),
(324, 33, '3', 0, 1),
(325, 33, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(328, 33, '1', 1, 0),
(331, 34, '1', 1, 0),
(332, 34, '1', 0, 1),
(333, 34, '3', 0, 1),
(334, 34, '3', 1, 0),
(335, 34, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(338, 34, '1', 0, -1),
(341, 35, '1', 0, 1),
(342, 35, '1', 1, 0),
(343, 35, '3', 0, 1),
(344, 35, '3', 0, -1),
(345, 35, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(348, 35, '1', 1, 0),
(351, 36, '1', 1, 0),
(352, 36, '1', 0, 1),
(353, 36, '3', 0, 1),
(354, 36, '3', 1, 0),
(358, 36, '1', 0, 1),
(361, 37, '1', 0, 1),
(362, 37, '1', 1, 0),
(363, 37, '3', 0, 1),
(364, 37, '3', 0, 1),
(365, 37, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(368, 37, '1', 1, 0),
(371, 38, '1', 1, 0),
(372, 38, '1', 0, 1),
(373, 38, '3', 0, 1),
(374, 38, '3', 1, 0),
(375, 38, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(378, 38, '1', 0, -1),
(381, 39, '1', 0, 1),
(382, 39, '1', 1, 0),
(383, 39, '3', 0, 1),
(384, 39, '3', 0, 1),
(385, 39, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 0),
(388, 39, '1', 1, 0),
(391, 40, '1', 1, 0),
(392, 40, '1', 0, 1),
(393, 40, '3', 0, 1),
(394, 40, '3', 1, 0),
(395, 40, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1),
(398, 40, '1', 0, -1),
(401, 36, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 1),
(409, 73, '1', 0, 1),
(410, 73, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 1);

-- --------------------------------------------------------

--
-- Table structure for table `profiles`
--

CREATE TABLE `profiles` (
  `user_id` char(36) NOT NULL,
  `username` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `profiles`
--

INSERT INTO `profiles` (`user_id`, `username`, `role`) VALUES
('1', 'saif', '0'),
('3', 'ahmed', '0'),
('6', 'abdsmad', '0'),
('6affa2df-dda9-442d-99ee-d2a3c1e78c64', 'hsan', 'admin'),
('8', 'coulibaly', '0');

-- --------------------------------------------------------

--
-- Table structure for table `replies`
--

CREATE TABLE `replies` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `thread_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` char(36) NOT NULL,
  `parent_id` bigint(20) UNSIGNED DEFAULT NULL,
  `content` text NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `replies`
--

INSERT INTO `replies` (`id`, `thread_id`, `user_id`, `parent_id`, `content`, `created_at`, `updated_at`) VALUES
(1, 1, '1', NULL, 'Thank you for creating this space. I\'ve been looking for a supportive mental health community.', '2024-01-15 10:30:00', '2026-02-23 17:31:11'),
(2, 1, '3', 1, 'Agreed! The guidelines look very thoughtful. Excited to be here.', '2024-01-15 11:45:00', '2024-01-15 11:45:00'),
(3, 2, '3', NULL, 'Hi everyone! I\'m a therapist looking to learn from others and share resources when appropriate.', '2024-01-16 14:20:00', '2026-02-23 17:32:40'),
(4, 2, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'New here too. Dealing with anxiety and hoping to find some practical tips.', '2024-01-16 15:10:00', '2026-02-28 18:20:16'),
(5, 3, '1', NULL, 'CBT changed my life! The key for me was consistent practice with thought records.', '2024-01-17 09:30:00', '2024-01-17 09:30:00'),
(6, 3, '1', 5, 'How long did it take before you noticed significant changes?', '2024-01-17 10:15:00', '2026-02-23 17:31:11'),
(7, 3, '1', 6, 'About 3 months of weekly therapy and daily exercises. It was gradual but steady.', '2024-01-17 11:00:00', '2024-01-17 11:00:00'),
(8, 4, '3', NULL, 'These worksheets are fantastic! The cognitive distortion list is especially helpful.', '2024-01-18 13:20:00', '2024-01-18 13:20:00'),
(9, 5, '1', NULL, 'The 5-4-3-2-1 grounding technique has been my go-to for anxiety attacks.', '2024-01-19 08:30:00', '2026-02-23 17:31:11'),
(10, 5, '3', 9, 'Can you explain how that works? Never heard of it.', '2024-01-19 09:15:00', '2026-02-23 17:32:40'),
(11, 5, '1', 10, '5 things you see, 4 you can touch, 3 you hear, 2 you smell, 1 you taste. Brings you to the present moment.', '2024-01-19 09:45:00', '2026-02-23 17:31:11'),
(12, 6, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'I love Headspace for beginners. The animations make it easy to understand.', '2024-01-20 16:20:00', '2026-02-28 18:20:16'),
(13, 6, '1', NULL, 'Insight Timer is free and has thousands of options. Highly recommend!', '2024-01-20 17:05:00', '2024-01-20 17:05:00'),
(14, 7, '3', NULL, 'EMDR was intense but ultimately very healing for me. Make sure you have a good therapist.', '2024-01-21 13:30:00', '2024-01-21 13:30:00'),
(15, 8, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Consistent goodbye routines helped my daughter. Same hug, same phrase, then leave promptly.', '2024-01-22 10:50:00', '2026-02-28 18:20:16'),
(16, 9, '1', NULL, 'Blackout curtains were a game changer for my sleep quality.', '2024-01-23 19:10:00', '2026-02-23 17:31:11'),
(17, 9, '3', 16, 'Yes! And keeping the room cool, around 65-68 degrees Fahrenheit.', '2024-01-23 19:45:00', '2026-02-23 17:32:40'),
(18, 10, '1', NULL, 'Congratulations on one year! That\'s an incredible achievement.', '2024-01-24 07:15:00', '2024-01-24 07:15:00'),
(19, 10, '3', NULL, 'So inspiring! Thank you for sharing your journey with us.', '2024-01-24 08:30:00', '2024-01-24 08:30:00'),
(20, 11, '3', NULL, 'The book \"The Brain That Changes Itself\" is a great resource on neuroplasticity.', '2024-01-25 15:40:00', '2026-02-23 17:32:40'),
(21, 12, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Active listening without planning your response has helped our marriage tremendously.', '2024-01-26 12:30:00', '2026-02-28 18:20:16'),
(22, 13, '1', NULL, 'Day 1: What made you smile today? Day 2: What challenged you? Simple prompts work best for me.', '2024-01-27 09:20:00', '2026-02-23 17:31:11'),
(23, 14, '1', NULL, 'Intuitive Eating by Tribole and Resch is the bible for this journey.', '2024-01-28 17:15:00', '2024-01-28 17:15:00'),
(24, 15, '3', NULL, 'Phone alarms for everything. I set multiple alarms with specific labels.', '2024-01-29 14:00:00', '2024-01-29 14:00:00'),
(25, 15, '3', 24, 'Same! \"LEAVE NOW or you\'ll be late\" is my most frequent alarm label.', '2024-01-29 14:30:00', '2026-02-23 17:32:40'),
(26, 16, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'I allow myself to feel both grief and joy. They can coexist during the holidays.', '2024-01-30 11:40:00', '2026-02-28 18:20:16'),
(27, 17, '1', NULL, 'Learning to say no without over-explaining has been crucial for my mental health.', '2024-01-31 08:45:00', '2026-02-23 17:31:11'),
(28, 18, '1', NULL, 'Thank you for the clear guidelines. This feels like a safe space.', '2024-02-01 09:30:00', '2024-02-01 09:30:00'),
(30, 19, '3', NULL, 'SSRIs helped me, but it took trying two different ones to find the right fit.', '2024-02-02 13:20:00', '2026-02-23 17:32:40'),
(31, 19, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 30, 'How did you know it was time to switch medications?', '2024-02-02 14:00:00', '2026-02-28 18:20:16'),
(32, 19, '3', 31, 'Side effects weren\'t improving after 8 weeks and minimal anxiety relief. Doctor guided the process.', '2024-02-02 14:45:00', '2026-02-23 17:32:40'),
(33, 20, '1', NULL, 'The Body Keeps the Score by Bessel van der Kolk is essential reading.', '2024-02-03 10:40:00', '2026-02-23 17:31:11'),
(34, 21, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Doing okay today. Grateful for this check-in thread.', '2024-02-05 09:15:00', '2026-02-28 18:20:16'),
(35, 22, '1', NULL, 'DBT adds acceptance and validation to CBT\'s change-focused approach. Both are valuable.', '2024-02-06 14:30:00', '2024-02-06 14:30:00'),
(36, 23, '3', NULL, 'Body scan meditations help me acknowledge pain without fighting it.', '2024-02-07 11:50:00', '2024-02-07 11:50:00'),
(37, 24, '3', NULL, 'I use DoesTheDogDie.com to check for triggers before watching anything.', '2024-02-08 16:10:00', '2026-02-23 17:32:40'),
(38, 25, '1', NULL, 'We implemented phone-free hours at home. It\'s made a noticeable difference.', '2024-02-09 10:30:00', '2026-02-23 17:31:11'),
(39, 26, '1', NULL, 'Night shift mode on all devices an hour before bed helped me fall asleep faster.', '2024-02-10 20:30:00', '2024-02-10 20:30:00'),
(40, 27, '3', NULL, 'Just be present and consistent. Show up without judgment.', '2024-02-11 13:45:00', '2024-02-11 13:45:00'),
(41, 28, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Dual n-back games have the most research backing for working memory improvement.', '2024-02-12 15:40:00', '2026-02-28 18:20:16'),
(42, 29, '1', NULL, 'Couples counseling gave us tools we couldn\'t find on our own. Highly recommend.', '2024-02-13 09:50:00', '2026-02-23 17:31:11'),
(43, 30, '3', NULL, 'I made a list of things I\'m naturally good at and found ways to use them more.', '2024-02-14 12:15:00', '2026-02-23 17:32:40'),
(44, 31, '1', NULL, 'Meal prepping with a friend makes it less overwhelming and more social.', '2024-02-15 17:20:00', '2024-02-15 17:20:00'),
(45, 32, '3', NULL, 'The Pomodoro Technique (25 min work, 5 min break) helps me start tasks.', '2024-02-16 08:30:00', '2024-02-16 08:30:00'),
(46, 33, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Grief counseling helped me understand that my timeline is valid, whatever it looks like.', '2024-02-17 14:45:00', '2026-02-28 18:20:16'),
(47, 34, '1', NULL, 'Having a dedicated workspace that I can physically leave at the end of day helps.', '2024-02-18 11:30:00', '2026-02-23 17:31:11'),
(48, 35, '3', NULL, 'Box breathing (4-4-4-4) helps calm the physical symptoms for me.', '2024-02-19 19:15:00', '2026-02-23 17:32:40'),
(49, 36, '1', NULL, 'No obligations day. I do exactly what I feel like in the moment.', '2024-02-20 10:45:00', '2024-02-20 10:45:00'),
(50, 37, '3', NULL, 'I use a simple app on my phone for quick thought records throughout the day.', '2024-02-21 13:30:00', '2024-02-21 13:30:00'),
(51, 38, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Start by walking slowly and paying attention to each foot lifting and touching the ground.', '2024-02-22 16:00:00', '2026-02-28 18:20:16'),
(52, 38, '1', 51, 'That sounds so simple but I never thought to do it intentionally. Thanks!', '2024-02-22 16:45:00', '2026-02-23 17:31:11'),
(53, 39, '1', NULL, 'My dreams become more vivid when I\'m stressed. It\'s a good warning system now.', '2024-02-23 08:20:00', '2024-02-23 08:20:00'),
(54, 40, '3', NULL, 'Learning a new language has been great for my memory and brain health.', '2024-02-24 12:40:00', '2026-02-23 17:32:40'),
(55, 1, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Are there any weekly virtual meetups planned? Would love to connect in real time.', '2024-02-25 09:15:00', '2026-02-28 18:20:16'),
(56, 2, '1', NULL, 'I\'m a psychology student hoping to learn from real experiences here.', '2024-02-25 13:30:00', '2024-02-25 13:30:00'),
(57, 3, '3', NULL, 'Exposure therapy combined with CBT was the winning formula for my social anxiety.', '2024-02-26 08:45:00', '2026-02-23 17:32:40'),
(58, 3, '1', 57, 'How did you start with exposure? Was it with a therapist?', '2024-02-26 09:30:00', '2026-02-23 17:31:11'),
(59, 3, '3', 58, 'Yes, therapist guided. Started with small things like making eye contact with cashiers.', '2024-02-26 10:15:00', '2026-02-23 17:32:40'),
(60, 4, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'The \"best friend test\" helps me challenge thoughts - would I say this to my best friend?', '2024-02-27 11:20:00', '2026-02-28 18:20:16'),
(61, 5, '3', NULL, 'Mindfulness isn\'t about emptying your mind, it\'s about noticing without judgment.', '2024-02-27 14:40:00', '2024-02-27 14:40:00'),
(62, 6, '1', NULL, 'Calm app has great sleep stories. Matthew McConaughey\'s voice is so relaxing!', '2024-02-28 07:50:00', '2026-02-23 17:31:11'),
(63, 7, '1', NULL, 'EMDR felt weird at first but the results were worth it. Stick with it.', '2024-02-28 18:30:00', '2024-02-28 18:30:00'),
(64, 8, '3', NULL, 'We made a goodbye song together. Sounds silly but it works!', '2024-02-29 09:10:00', '2026-02-23 17:32:40'),
(65, 8, '3', 64, 'Not silly at all if it works! Kids respond to routines and rituals.', '2024-02-29 10:05:00', '2024-02-29 10:05:00'),
(66, 9, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'White noise machine blocks out neighborhood sounds that used to wake me.', '2024-03-01 20:30:00', '2026-02-28 18:20:16'),
(67, 10, '1', NULL, 'Day 30 here. Your post gives me hope for reaching one year.', '2024-03-02 07:20:00', '2026-02-23 17:31:11'),
(68, 10, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 67, 'You can do this! One day at a time. Reach out if you need support.', '2024-03-02 08:15:00', '2026-02-28 18:20:16'),
(69, 11, '1', NULL, 'Meditation literally changes brain structure over time. The research is fascinating.', '2024-03-03 13:50:00', '2024-03-03 13:50:00'),
(70, 12, '3', NULL, 'We have a weekly check-in where we both share feelings without interruption.', '2024-03-03 16:30:00', '2024-03-03 16:30:00'),
(71, 13, '3', NULL, 'I photograph one thing I\'m grateful for each day. Visual gratitude works for me.', '2024-03-04 08:40:00', '2026-02-23 17:32:40'),
(72, 14, '1', NULL, 'Food is not the enemy. Still working on fully believing that.', '2024-03-04 19:15:00', '2026-02-23 17:31:11'),
(73, 15, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Visual timers help me. I use one for everything now.', '2024-03-05 12:20:00', '2026-02-28 18:20:16'),
(74, 16, '1', NULL, 'I light a candle for my loved one during holiday meals. Keeps them present.', '2024-03-05 15:45:00', '2024-03-05 15:45:00'),
(75, 17, '3', NULL, 'Turning off work notifications after hours was non-negotiable for my sanity.', '2024-03-06 09:30:00', '2024-03-06 09:30:00'),
(76, 18, '1', NULL, 'The moderation team seems really active and caring. Appreciate you all.', '2024-03-06 21:10:00', '2026-02-23 17:31:11'),
(77, 19, '3', NULL, 'Beta blockers work well for my situational anxiety without the side effects of SSRIs.', '2024-03-07 14:20:00', '2026-02-23 17:32:40'),
(78, 20, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Complex PTSD by Pete Walker saved my life. Cannot recommend enough.', '2024-03-07 17:50:00', '2026-02-28 18:20:16'),
(79, 21, '1', NULL, 'Struggling this week but reading these replies helps.', '2024-03-08 10:35:00', '2026-02-23 17:31:11'),
(80, 21, '3', 79, 'Proud of you for being here. Small steps count.', '2024-03-08 11:20:00', '2026-02-23 17:32:40'),
(81, 22, '3', NULL, 'DBT skills like distress tolerance have been more useful for me than pure CBT.', '2024-03-08 15:10:00', '2024-03-08 15:10:00'),
(82, 23, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Yoga nidra helps me relax even when pain is high. Look it up on YouTube.', '2024-03-09 08:50:00', '2026-02-28 18:20:16'),
(83, 24, '1', NULL, 'I give myself permission to leave the room or turn off anything triggering.', '2024-03-09 20:15:00', '2024-03-09 20:15:00'),
(84, 25, '3', NULL, 'Modeling healthy phone use ourselves is the hardest but most important part.', '2024-03-10 13:30:00', '2024-03-10 13:30:00'),
(85, 26, '3', NULL, 'Magnesium supplements before bed improved my sleep quality noticeably.', '2024-03-10 22:40:00', '2026-02-23 17:32:40'),
(86, 27, '1', NULL, 'Al-Anon meetings helped me learn to support without enabling.', '2024-03-11 12:15:00', '2026-02-23 17:31:11'),
(87, 28, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Chess and strategy games keep my mind sharp more than brain training apps.', '2024-03-11 16:50:00', '2026-02-28 18:20:16'),
(88, 29, '1', NULL, 'Transparency and patience. It takes time but it is possible to rebuild.', '2024-03-12 09:25:00', '2024-03-12 09:25:00'),
(89, 30, '3', NULL, 'My strength is empathy. I found a volunteer role where that matters.', '2024-03-12 18:30:00', '2024-03-12 18:30:00'),
(90, 31, '3', NULL, 'Smoothies when I can\'t face meals. Easier to get nutrition in liquid form.', '2024-03-13 11:45:00', '2026-02-23 17:32:40'),
(91, 32, '1', NULL, 'Body doubling helps me - working alongside someone else, even virtually.', '2024-03-13 15:20:00', '2026-02-23 17:31:11'),
(92, 33, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Grief has no timeline. Be gentle with yourself.', '2024-03-14 08:10:00', '2026-02-28 18:20:16'),
(93, 34, '1', NULL, 'Morning walks before checking email sets a better tone for my day.', '2024-03-14 14:45:00', '2024-03-14 14:45:00'),
(94, 35, '3', NULL, 'Cold water on my wrists helps calm my nervous system during panic.', '2024-03-15 19:30:00', '2024-03-15 19:30:00'),
(95, 36, '3', NULL, 'No alarm, comfy clothes, and a good book. Perfect mental health day.', '2024-03-15 21:15:00', '2026-02-23 17:32:40'),
(96, 37, '1', NULL, 'I use a notes app folder just for thought records. Easy to review patterns.', '2024-03-16 10:40:00', '2026-02-23 17:31:11'),
(97, 38, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Try matching your breath to your steps. In for 4 steps, out for 4 steps.', '2024-03-16 14:20:00', '2026-02-28 18:20:16'),
(98, 39, '1', NULL, 'My nightmares decreased when I stopped eating close to bedtime.', '2024-03-17 07:50:00', '2024-03-17 07:50:00'),
(99, 40, '3', NULL, 'Sudoku and crossword puzzles are my daily brain exercise.', '2024-03-17 18:35:00', '2024-03-17 18:35:00'),
(100, 2, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'Late to introducing myself but hi everyone! Grateful for this space.', '2024-03-18 12:10:00', '2026-02-28 18:20:16'),
(110, 18, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 76, 'comment test', '2026-02-16 23:52:52', '2026-02-28 18:20:16'),
(114, 18, '6affa2df-dda9-442d-99ee-d2a3c1e78c64', NULL, 'ggudsgj', '2026-02-17 15:45:33', '2026-02-28 18:20:16'),
(120, 18, '1', NULL, 'notification test', '2026-02-22 21:34:09', '2026-02-22 21:34:09');

-- --------------------------------------------------------

--
-- Table structure for table `threads`
--

CREATE TABLE `threads` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `category_id` bigint(20) UNSIGNED NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `type` enum('discussion','question','announcement') DEFAULT 'discussion',
  `status` enum('open','locked','archived','hidden') DEFAULT 'open',
  `is_pinned` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `user_id` char(36) NOT NULL,
  `likecount` int(11) DEFAULT 0,
  `dislikecount` int(11) DEFAULT 0,
  `followcount` int(11) DEFAULT 0,
  `repliescount` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `threads`
--

INSERT INTO `threads` (`id`, `category_id`, `title`, `content`, `image_url`, `type`, `status`, `is_pinned`, `created_at`, `updated_at`, `user_id`, `likecount`, `dislikecount`, `followcount`, `repliescount`) VALUES
(1, 1, 'Welcome to Our Mental Health Community', 'We\'re glad you\'re here. This is a safe space to discuss anything related to mental health. Please read our community guidelines before posting.', NULL, 'announcement', 'open', 0, '2024-01-15 09:00:00', '2026-02-16 16:34:25', '1', 4, 2, 4, 3),
(2, 1, 'Introduce Yourself Thread', 'New here? Tell us a bit about yourself and what brings you to our community.', NULL, 'discussion', 'open', 0, '2024-01-16 13:30:00', '2026-02-23 17:30:47', '1', 5, 2, 3, 4),
(3, 2, 'CBT for Social Anxiety - Success Stories?', 'Has anyone used CBT techniques to manage social anxiety? I\'d love to hear what worked for you.', NULL, 'question', 'open', 0, '2024-01-17 08:15:00', '2026-02-16 16:34:25', '3', 4, 2, 4, 6),
(4, 2, 'Cognitive Restructuring Worksheets', 'Sharing a collection of CBT worksheets I\'ve found helpful for challenging negative thoughts. Feel free to add your own!', NULL, 'discussion', 'open', 0, '2024-01-18 10:20:00', '2026-02-23 17:32:26', '3', 5, 2, 3, 2),
(5, 3, 'Beginner\'s Guide to Mindfulness', 'Just starting with mindfulness? Here are 5 simple exercises you can try today, even with a busy schedule.', NULL, 'discussion', 'open', 0, '2024-01-19 07:45:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 2, 3, 4),
(6, 3, 'Apps for Guided Meditation', 'What meditation apps do you recommend? Looking for both free and paid options.', NULL, 'question', 'open', 0, '2024-01-20 15:30:00', '2026-02-16 16:34:25', '1', 5, 1, 4, 3),
(7, 4, 'EMDR Therapy Experiences', 'I\'m considering EMDR for trauma recovery. Can anyone share their experiences with this type of therapy?', NULL, 'question', 'open', 0, '2024-01-21 12:10:00', '2026-02-23 17:30:47', '1', 5, 2, 3, 2),
(8, 5, 'Helping Children with Separation Anxiety', 'My 7-year-old struggles with separation anxiety at school drop-off. Looking for gentle strategies that have worked for others.', NULL, 'discussion', 'open', 0, '2024-01-22 09:40:00', '2026-02-16 16:34:25', '3', 5, 1, 4, 3),
(9, 6, 'Creating the Perfect Sleep Environment', 'Let\'s share tips on optimizing bedrooms for better sleep - temperature, lighting, sound, and bedding.', NULL, 'discussion', 'open', 0, '2024-01-23 18:15:00', '2026-02-23 17:32:26', '3', 6, 1, 3, 3),
(10, 7, 'One Year Sober - My Journey', 'Celebrating one year of recovery today. Sharing my story in hopes it might inspire others on this path.', NULL, 'discussion', 'open', 0, '2024-01-24 06:30:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 1, 4, 4),
(11, 8, 'Understanding Neuroplasticity', 'A deep dive into how our brains can rewire themselves throughout life. Resources and discussion inside.', NULL, 'discussion', 'open', 0, '2024-01-25 14:20:00', '2026-02-16 16:34:25', '1', 6, 1, 3, 2),
(12, 9, 'Communication Tools for Couples', 'The \"I feel\" statements technique has transformed how my partner and I discuss difficult topics. What works for you?', NULL, 'discussion', 'open', 0, '2024-01-26 11:45:00', '2026-02-23 17:30:47', '1', 5, 1, 4, 2),
(13, 10, 'Gratitude Journal Prompts', 'Starting a gratitude practice? Here are 30 days of journal prompts to get you started.', NULL, 'discussion', 'open', 0, '2024-01-27 08:00:00', '2026-02-16 16:34:25', '3', 6, 1, 3, 2),
(14, 11, 'Intuitive Eating Resources', 'Moving away from diet culture and toward intuitive eating. Book recommendations and support welcome.', NULL, 'discussion', 'open', 0, '2024-01-28 16:30:00', '2026-02-23 17:32:26', '3', 5, 1, 4, 2),
(15, 12, 'ADHD and Time Blindness - Tips?', 'I struggle with time blindness and am always late. What strategies have helped you manage this?', NULL, 'question', 'open', 0, '2024-01-29 13:10:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 6, 1, 3, 3),
(16, 13, 'Navigating Holidays After a Loss', 'First holiday season since losing my mother. How do others cope with grief during celebratory times?', NULL, 'discussion', 'open', 0, '2024-01-30 10:25:00', '2026-02-16 16:34:25', '1', 5, 1, 4, 2),
(17, 14, 'Setting Boundaries at Work', 'How do you maintain mental health while working in a high-stress environment? Let\'s share boundary-setting strategies.', NULL, 'discussion', 'open', 0, '2024-01-31 07:50:00', '2026-02-23 17:30:47', '1', 6, 1, 3, 2),
(18, 15, 'COMMUNITY GUIDELINES - PLEASE READ', 'Welcome to our Anxiety Management community! Please review our guidelines for creating a safe, supportive environment for everyone.', NULL, 'announcement', 'open', 1, '2024-02-01 08:00:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 2, 5, 4),
(19, 15, 'Medication for Anxiety - Experiences?', 'Considering medication for generalized anxiety. Would love to hear about others\' experiences with different options.', NULL, 'question', 'open', 0, '2024-02-02 12:15:00', '2026-02-16 16:34:25', '3', 6, 1, 3, 4),
(20, 4, 'Books That Helped Me Process Trauma', 'A list of books that supported my trauma recovery journey. Please add your recommendations below.', NULL, 'discussion', 'open', 0, '2024-02-03 09:30:00', '2026-02-23 17:32:26', '3', 5, 1, 4, 2),
(21, 1, 'Weekend Check-In', 'How is everyone doing this weekend? Share your wins, struggles, or just check in with the community.', NULL, 'discussion', 'open', 0, '2024-02-05 08:30:00', '2026-02-16 16:34:25', '3', 6, 1, 3, 3),
(22, 2, 'CBT vs DBT - What\'s the Difference?', 'I\'m trying to understand the main differences between Cognitive Behavioral Therapy and Dialectical Behavior Therapy. Can someone explain?', NULL, 'question', 'open', 0, '2024-02-06 13:15:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 1, 4, 2),
(23, 3, 'Mindfulness for Chronic Pain', 'Has anyone used mindfulness techniques to manage chronic pain? Looking for resources and personal experiences.', NULL, 'discussion', 'open', 0, '2024-02-07 10:40:00', '2026-02-23 17:30:47', '1', 6, 1, 3, 2),
(24, 4, 'Triggered by Movies - Coping Strategies', 'Certain movies unexpectedly trigger my trauma. How do you handle triggers in media and entertainment?', NULL, 'question', 'open', 0, '2024-02-08 15:20:00', '2026-02-23 17:32:26', '3', 5, 1, 4, 2),
(25, 5, 'Teenage Anxiety and Social Media', 'My 14-year-old\'s anxiety seems linked to social media use. How do other parents navigate this?', NULL, 'discussion', 'open', 0, '2024-02-09 09:10:00', '2026-02-16 16:34:25', '1', 6, 1, 3, 2),
(26, 6, 'Blue Light and Sleep Quality', 'I\'ve been trying to reduce screen time before bed. Has anyone noticed improvements in their sleep after cutting blue light?', NULL, 'discussion', 'open', 0, '2024-02-10 19:45:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 1, 4, 2),
(27, 7, 'Supporting a Loved One in Recovery', 'My brother is in early recovery. How can I best support him without being overbearing?', NULL, 'question', 'open', 0, '2024-02-11 12:30:00', '2026-02-23 17:30:47', '1', 6, 1, 3, 2),
(28, 8, 'Brain Training Apps - Do They Work?', 'Are apps like Lumosity actually effective for cognitive function, or are they just games?', NULL, 'question', 'open', 0, '2024-02-12 14:50:00', '2026-02-16 16:34:25', '3', 5, 1, 4, 2),
(29, 9, 'Rebuilding Trust After Betrayal', 'My partner and I are working through infidelity. Any resources for rebuilding trust in a relationship?', NULL, 'discussion', 'open', 0, '2024-02-13 08:25:00', '2026-02-23 17:32:26', '3', 6, 1, 3, 2),
(30, 10, 'Strength-Based Goal Setting', 'Instead of focusing on fixing weaknesses, how do you set goals based on your strengths?', NULL, 'discussion', 'open', 0, '2024-02-14 11:00:00', '2026-02-16 16:34:25', '1', 5, 1, 4, 2),
(31, 11, 'Meal Planning with Eating Disorder Recovery', 'Meal planning feels overwhelming during recovery. Anyone have simple strategies that work for them?', NULL, 'question', 'open', 0, '2024-02-15 16:35:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 6, 1, 3, 2),
(32, 12, 'ADHD and Procrastination', 'The procrastination cycle is killing my productivity. What techniques actually help with ADHD-related procrastination?', NULL, 'discussion', 'open', 0, '2024-02-16 07:20:00', '2026-02-23 17:30:47', '1', 5, 1, 4, 2),
(33, 13, 'Complicated Grief - When Mourning Doesn\'t End', 'It\'s been two years and my grief hasn\'t eased. How do you know when it\'s complicated grief versus normal mourning?', NULL, 'question', 'open', 0, '2024-02-17 13:55:00', '2026-02-16 16:34:25', '3', 6, 1, 3, 2),
(34, 14, 'Remote Work and Mental Health', 'Working from home has blurred my work-life boundaries. How do you maintain separation when your home is your office?', NULL, 'discussion', 'open', 0, '2024-02-18 10:15:00', '2026-02-23 17:32:26', '3', 5, 1, 4, 2),
(35, 15, 'Anxiety and Physical Symptoms', 'My anxiety manifests as chest tightness and shortness of breath. Anyone else experience physical symptoms?', NULL, 'discussion', 'open', 0, '2024-02-19 18:40:00', '2026-02-16 16:34:25', '1', 6, 1, 3, 2),
(36, 1, 'Mental Health Days - How Do You Spend Yours?', 'When you take a mental health day, what does your ideal day look like? Looking for self-care ideas.', NULL, 'discussion', 'archived', 0, '2024-02-20 09:30:00', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 5, 1, 5, 2),
(37, 2, 'Thought Records - Share Your Template', 'I\'m trying different CBT thought record formats. What template works best for you? Please share examples.', NULL, 'discussion', 'open', 0, '2024-02-21 12:45:00', '2026-02-23 17:30:47', '1', 6, 1, 3, 2),
(38, 3, 'Mindful Walking - How to Practice', 'I want to try mindful walking but not sure where to start. Any guidance for beginners?', NULL, 'question', 'open', 0, '2024-02-22 15:10:00', '2026-02-16 16:34:25', '3', 5, 1, 4, 3),
(39, 6, 'Dream Journaling Benefits', 'I started keeping a dream journal and noticed patterns related to my stress levels. Anyone else track their dreams?', NULL, 'discussion', 'open', 0, '2024-02-23 07:05:00', '2026-02-23 17:32:26', '3', 6, 1, 3, 2),
(40, 8, 'Memory Techniques for Brain Health', 'As I age, I worry about memory. What techniques do you use to keep your memory sharp?', NULL, 'discussion', 'open', 0, '2024-02-24 11:50:00', '2026-02-16 16:34:25', '1', 5, 1, 4, 2),
(73, 1, 'this is new thread', 'new thread test test', NULL, 'discussion', 'open', 0, '2026-02-17 15:48:48', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 2, 0, 0, 0),
(76, 3, '5-minute guided mindfulness for beginners', 'I recorded a short mindfulness exercise that\'s perfect for those just starting out. It focuses on breath awareness and body scanning.', NULL, 'announcement', 'open', 0, '2026-02-10 19:36:20', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 22, 0, 44, 5),
(79, 6, 'Finally fixed my sleep schedule after years', 'I struggled with insomnia for over a decade. Here\'s exactly what worked for me: morning sunlight, no screens 2 hours before bed, and this specific breathing technique...', NULL, 'discussion', 'open', 0, '2026-01-18 19:36:20', '2026-02-23 17:34:42', '1', 92, 0, 30, 20),
(81, 8, 'Question about neuroplasticity and anxiety', 'I read that we can rewire our brains at any age. How exactly does this work for anxiety disorders? Looking for scientific explanations.', NULL, 'question', 'open', 0, '2026-02-17 19:36:20', '2026-02-23 17:32:26', '3', 17, 4, 2, 10),
(83, 10, 'Gratitude journaling actually works?', 'I\'ve been skeptical about positive psychology interventions. Has gratitude journaling made a real difference in anyone\'s life?', NULL, 'question', 'open', 0, '2026-02-17 19:36:20', '2026-02-23 17:34:42', '1', 24, 2, 6, 24),
(84, 11, 'Recovery from binge eating - what helped you?', 'I\'m in the early stages of recovery and struggling with the urge to binge during stress. Looking for practical tips that have helped others.', NULL, 'discussion', 'open', 0, '2026-02-03 19:36:20', '2026-02-23 17:30:47', '1', 35, 3, 16, 4),
(85, 12, 'ADHD-friendly organization system', 'I created a simple color-coded system that actually works with my ADHD brain. Sharing templates and photos of how I set it up.', NULL, 'discussion', 'open', 0, '2026-01-19 19:36:20', '2026-02-20 19:40:09', '3', 41, 0, 10, 8),
(87, 14, 'Setting boundaries at work without guilt', 'I\'m a people-pleaser and struggle to say no. How do you maintain mental health while being professional?', NULL, 'discussion', 'open', 0, '2026-02-09 19:36:20', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 1, 8, 23, 13),
(88, 15, 'Panic attack stopped me from driving', 'Had a severe panic attack while driving on the highway. Now I\'m scared to get behind the wheel. Has anyone overcome driving anxiety?', NULL, 'discussion', 'open', 0, '2026-02-19 19:36:20', '2026-02-23 17:30:47', '1', 1, 5, 25, 41),
(89, 3, 'Free virtual mindfulness retreat next weekend', 'Organizing a free 2-hour online mindfulness session. Everyone welcome, especially beginners! Link and schedule inside.', NULL, 'announcement', 'open', 0, '2026-02-17 19:36:20', '2026-02-23 17:32:26', '3', 36, 1, 22, 5),
(90, 2, 'CBT vs DBT - which helped you more?', 'I\'m trying to decide between therapists who specialize in different modalities. For those with experience in both, what were the pros and cons?', NULL, 'question', 'open', 0, '2026-02-08 19:36:20', '2026-02-23 17:34:42', '1', 10, 0, 8, 19),
(91, 4, 'Somatic experiencing for trauma', 'Started somatic therapy after years of talk therapy. The body-based approach is bringing up things I never accessed before. Anyone else doing SE?', NULL, 'discussion', 'open', 0, '2026-02-07 19:36:20', '2026-02-19 19:36:20', '1', 15, 1, 11, 13),
(92, 9, 'How to rebuild trust after betrayal', 'Partner had an emotional affair. We\'re trying to work through it but trust is shattered. Any success stories or resources?', NULL, 'discussion', 'locked', 0, '2026-01-13 19:36:20', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 51, 2, 9, 46),
(97, 1, 'Best practices for writing clean code', 'I wanted to share some tips I\'ve learned about writing maintainable Java code. Using proper naming conventions and keeping methods short really helps the team.', NULL, 'discussion', 'open', 0, '2026-02-21 17:02:55', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 0, 0, 0),
(102, 1, 'You are all stupid idiots', 'Everyone in this forum is an absolute moron and I hate all of you, you\'re worthless garbage and should shut up forever.', NULL, 'discussion', 'open', 0, '2026-02-21 18:31:05', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 0, 0, 0),
(104, 1, 'i want to kill everyone', 'i want to kill everyone because i hate them all', NULL, 'discussion', 'open', 0, '2026-02-21 20:27:06', '2026-02-28 18:20:01', '6affa2df-dda9-442d-99ee-d2a3c1e78c64', 0, 0, 0, 0);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `slug` (`slug`),
  ADD KEY `fk_categories_parent` (`parent_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `notifications_ibfk_1` (`thread_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `postinteraction`
--
ALTER TABLE `postinteraction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `thread_id` (`thread_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `profiles`
--
ALTER TABLE `profiles`
  ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `replies`
--
ALTER TABLE `replies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_reply_thread` (`thread_id`),
  ADD KEY `fk_reply_parent` (`parent_id`),
  ADD KEY `fk_reply_user` (`user_id`);

--
-- Indexes for table `threads`
--
ALTER TABLE `threads`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_thread_categorysecondary` (`category_id`),
  ADD KEY `fk_thread_user` (`user_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=75;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `postinteraction`
--
ALTER TABLE `postinteraction`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=411;

--
-- AUTO_INCREMENT for table `replies`
--
ALTER TABLE `replies`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=121;

--
-- AUTO_INCREMENT for table `threads`
--
ALTER TABLE `threads`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=105;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `categories`
--
ALTER TABLE `categories`
  ADD CONSTRAINT `fk_categories_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`thread_id`) REFERENCES `threads` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `postinteraction`
--
ALTER TABLE `postinteraction`
  ADD CONSTRAINT `fk_postinteraction_thread` FOREIGN KEY (`thread_id`) REFERENCES `threads` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `postinteraction_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `replies`
--
ALTER TABLE `replies`
  ADD CONSTRAINT `fk_reply_parent` FOREIGN KEY (`parent_id`) REFERENCES `replies` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_reply_thread` FOREIGN KEY (`thread_id`) REFERENCES `threads` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `threads`
--
ALTER TABLE `threads`
  ADD CONSTRAINT `fk_thread_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  ADD CONSTRAINT `fk_thread_categorysecondary` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_thread_user` FOREIGN KEY (`user_id`) REFERENCES `profiles` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
