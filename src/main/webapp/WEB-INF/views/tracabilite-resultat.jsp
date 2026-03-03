<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Résultat Traçabilité - Back-office Hôtel</title>
    <style>
        :root {
            --primary-color: #4299e1;
            --primary-dark: #3182ce;
            --secondary-color: #e2e8f0;
            --accent-color: #48bb78;
            --error-color: #f56565;
            --text-primary: #2d3748;
            --text-secondary: #4a5568;
            --bg-gradient-start: #f7fafc;
            --bg-gradient-end: #edf2f7;
            --shadow-light: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            --shadow-medium: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            --shadow-hover: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            --border-radius: 12px;
            --border-radius-large: 16px;
        }
        * {
            box-sizing: border-box;
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        }
        body {
            background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
            min-height: 100vh;
            margin: 0;
            padding: 24px;
            color: var(--text-primary);
            line-height: 1.6;
        }
        .container {
            max-width: 640px;
            margin: 0 auto;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(20px);
            border-radius: var(--border-radius-large);
            box-shadow: var(--shadow-medium);
            padding: 48px;
            border: 1px solid rgba(255, 255, 255, 0.2);
            animation: fadeInUp 0.6s ease-out;
            text-align: center;
        }
        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        h1 {
            color: var(--text-primary);
            text-align: center;
            margin-bottom: 24px;
            font-size: 32px;
            font-weight: 800;
            letter-spacing: -0.025em;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .date-display {
            font-size: 48px;
            font-weight: 800;
            color: var(--primary-dark);
            margin: 40px 0;
            padding: 32px;
            background: linear-gradient(135deg, rgba(66, 153, 225, 0.08) 0%, rgba(49, 130, 206, 0.15) 100%);
            border-radius: var(--border-radius-large);
            border: 2px solid rgba(66, 153, 225, 0.2);
            letter-spacing: 0.02em;
        }
        .back-link {
            display: inline-block;
            margin-top: 32px;
            padding: 18px 36px;
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
            color: white;
            text-decoration: none;
            border-radius: var(--border-radius);
            font-size: 16px;
            font-weight: 700;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
        }
        .back-link::before {
            content: '';
            position: absolute;
            top: 0;
            left: -100%;
            width: 100%;
            height: 100%;
            background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
            transition: left 0.5s;
        }
        .back-link:hover::before {
            left: 100%;
        }
        .back-link:hover {
            transform: translateY(-3px);
            box-shadow: var(--shadow-hover);
        }
        @media (max-width: 768px) {
            .container {
                padding: 32px 24px;
            }
            h1 {
                font-size: 28px;
            }
            .date-display {
                font-size: 32px;
                padding: 24px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚐 Traçabilité des Véhicules</h1>

        <div class="date-display">
            📅 ${date}
        </div>

        <a href="${pageContext.request.contextPath}/tracabilite" class="back-link">⬅ Retour</a>
    </div>
</body>
</html>
