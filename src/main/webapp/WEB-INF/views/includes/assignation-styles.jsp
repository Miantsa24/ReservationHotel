<style>
    :root {
        --primary-color: #4299e1;
        --primary-dark: #3182ce;
        --primary-light: #818cf8;
        --secondary-color: #e2e8f0;
        --accent-color: #10b981;
        --accent-dark: #059669;
        --accent-light: #34d399;
        --warning-color: #f59e0b;
        --error-color: #ef4444;
        --success-color: #10b981;
        --text-primary: #1e293b;
        --text-secondary: #64748b;
        --bg-gradient-start: #f8fafc;
        --bg-gradient-end: #e2e8f0;
        --card-bg: rgba(255, 255, 255, 0.9);
        --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
        --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
        --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
        --border-radius: 12px;
        --border-radius-lg: 16px;
        --border-radius-xl: 24px;
    }
    * { margin: 0; padding: 0; box-sizing: border-box; }
    body {
        font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
        background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
        min-height: 100vh;
        color: var(--text-primary);
        line-height: 1.6;
    }
    .app-layout { display: flex; gap: 28px; max-width: 1400px; margin: 0 auto; padding: 24px; min-height: 100vh; align-items: flex-start; }
    .main-content { flex: 1; min-width: 0; }
    .container { max-width: 1200px; margin: 0 auto; }

    @keyframes slideDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes scaleIn { from { opacity: 0; transform: scale(0.9); } to { opacity: 1; transform: scale(1); } }

    .page-header { margin-bottom: 32px; animation: slideDown 0.5s ease-out; }
    .page-title { font-size: 32px; font-weight: 800; background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; margin-bottom: 8px; }
    .page-subtitle { color: var(--text-secondary); font-size: 16px; font-weight: 500; }
    .date-header { color: var(--text-secondary); font-size: 16px; margin-top: 8px; }

    .breadcrumb { display: flex; align-items: center; gap: 8px; margin-bottom: 24px; font-size: 14px; animation: slideDown 0.4s ease-out; }
    .breadcrumb a { color: var(--primary-color); text-decoration: none; font-weight: 600; transition: color 0.2s; }
    .breadcrumb a:hover { color: var(--primary-dark); }
    .breadcrumb span { color: var(--text-secondary); }

    .date-badge { display: inline-flex; align-items: center; gap: 8px; background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); color: white; padding: 10px 20px; border-radius: var(--border-radius); font-weight: 700; font-size: 15px; margin-bottom: 24px; box-shadow: var(--shadow-md); }

    .card { background: var(--card-bg); backdrop-filter: blur(20px); border-radius: var(--border-radius-xl); padding: 32px; box-shadow: var(--shadow-lg); border: 1px solid rgba(255, 255, 255, 0.5); animation: slideUp 0.6s ease-out; }
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid rgba(0,0,0,0.06); }
    .card-title { font-size: 20px; font-weight: 700; color: var(--text-primary); }
    .card-count { background: var(--primary-color); color: white; padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 600; }

    /* Info badges */
    .info-badges { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
    .info-badge { display: inline-flex; align-items: center; gap: 6px; padding: 8px 16px; border-radius: var(--border-radius); font-weight: 600; font-size: 14px; }
    .info-badge.date { background: rgba(66, 153, 225, 0.1); color: var(--primary-dark); }
    .info-badge.time { background: rgba(16, 185, 129, 0.1); color: var(--accent-dark); }

    /* Dashboard */
    .dashboard { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 28px; }
    .dashboard-card { padding: 20px; border-radius: var(--border-radius-lg); text-align: center; transition: transform 0.2s, box-shadow 0.2s; }
    .dashboard-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }
    .dashboard-card.success { background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.2) 100%); border: 1px solid rgba(16, 185, 129, 0.3); }
    .dashboard-card.warning { background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.2) 100%); border: 1px solid rgba(239, 68, 68, 0.3); }
    .dashboard-card.info { background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.2) 100%); border: 1px solid rgba(245, 158, 11, 0.3); }
    .dashboard-card.primary { background: linear-gradient(135deg, rgba(66, 153, 225, 0.1) 0%, rgba(66, 153, 225, 0.2) 100%); border: 1px solid rgba(66, 153, 225, 0.3); }
    .dashboard-icon { font-size: 28px; margin-bottom: 8px; }
    .dashboard-value { font-size: 32px; font-weight: 800; color: var(--text-primary); }
    .dashboard-label { font-size: 13px; color: var(--text-secondary); font-weight: 500; margin-top: 4px; }

    /* Vehicule card (tracabilite) */
    .vehicule-card { background: var(--card-bg); border-radius: var(--border-radius-xl); padding: 24px; margin-bottom: 20px; box-shadow: var(--shadow-md); border: 1px solid rgba(0,0,0,0.04); animation: slideUp 0.5s ease-out; }
    .vehicule-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
    .vehicule-name { font-size: 22px; font-weight: 800; color: var(--text-primary); }
    .vehicule-tags { display: flex; gap: 8px; flex-wrap: wrap; }
    .tag { padding: 6px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
    .tag-capacite { background: rgba(66, 153, 225, 0.15); color: var(--primary-dark); }
    .tag-carburant { background: rgba(16, 185, 129, 0.15); color: var(--accent-dark); }
    .tag-vitesse { background: rgba(245, 158, 11, 0.15); color: #b45309; }

    /* Info grid */
    .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; margin-bottom: 20px; }
    .info-box { background: rgba(0,0,0,0.02); padding: 16px; border-radius: var(--border-radius); text-align: center; }
    .info-box-label { font-size: 12px; color: var(--text-secondary); font-weight: 500; margin-bottom: 6px; }
    .info-box-value { font-size: 18px; font-weight: 700; color: var(--text-primary); }

    /* Section title */
    .section-title { font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 12px; margin-top: 20px; }

    /* Parcours */
    .parcours-container { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; padding: 16px; background: rgba(0,0,0,0.02); border-radius: var(--border-radius); }
    .parcours-step { padding: 8px 14px; background: white; border-radius: 8px; font-weight: 600; font-size: 13px; box-shadow: var(--shadow-sm); }
    .parcours-arrow { color: var(--text-secondary); font-weight: 700; }

    /* Badges */
    .badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: 600; }
    .badge-id { background: rgba(66, 153, 225, 0.15); color: var(--primary-dark); }
    .badge-persons { background: rgba(16, 185, 129, 0.15); color: var(--accent-dark); }

    /* Tables */
    .table-container { overflow-x: auto; }
    .table-container table, table { width: 100%; border-collapse: collapse; }
    .table-container th, .table-container td, table th, table td { padding: 12px 14px; text-align: left; border-bottom: 1px solid rgba(0,0,0,0.06); }
    .table-container th, table th { font-weight: 600; color: var(--text-secondary); font-size: 13px; text-transform: uppercase; letter-spacing: 0.5px; }
    .table-container tbody tr:hover, table tbody tr:hover { background: rgba(0,0,0,0.02); }

    /* Vehicle grid (assignation-detail) */
    .vehicle-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; margin-top: 16px; }
    .vehicle-card { background: linear-gradient(135deg, rgba(66, 153, 225, 0.05) 0%, rgba(49, 130, 206, 0.1) 100%); border: 1px solid rgba(66, 153, 225, 0.2); border-radius: var(--border-radius-lg); padding: 20px; }
    .vehicle-title { font-size: 16px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
    .vehicle-meta { font-size: 12px; color: var(--text-secondary); margin-bottom: 12px; }
    .vehicle-reservations { margin-bottom: 12px; }
    .reservation-chip { display: inline-flex; align-items: center; gap: 6px; padding: 6px 10px; background: white; border-radius: 20px; font-size: 12px; margin: 4px 4px 4px 0; box-shadow: var(--shadow-sm); }
    .vehicle-stats { display: flex; flex-direction: column; gap: 6px; padding-top: 12px; border-top: 1px solid rgba(0,0,0,0.06); }
    .vehicle-stats .stat { font-size: 13px; color: var(--text-secondary); }
    .vehicle-stats .stat strong { color: var(--text-primary); }

    /* Stats grid (assignation-result) */
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 16px; margin: 24px 0; }
    .stat-card { padding: 24px; border-radius: var(--border-radius-lg); text-align: center; }
    .stat-card.success { background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.2) 100%); border: 1px solid rgba(16, 185, 129, 0.3); }
    .stat-card.warning { background: linear-gradient(135deg, rgba(245, 158, 11, 0.1) 0%, rgba(245, 158, 11, 0.2) 100%); border: 1px solid rgba(245, 158, 11, 0.3); }
    .stat-value { font-size: 36px; font-weight: 800; color: var(--text-primary); }
    .stat-label { font-size: 14px; color: var(--text-secondary); font-weight: 500; margin-top: 4px; }

    /* Success icon */
    .success-icon { width: 64px; height: 64px; background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-dark) 100%); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 32px; color: white; margin: 0 auto 16px; box-shadow: var(--shadow-lg); }

    /* Alerts */
    .alert-error { display: flex; align-items: center; gap: 12px; padding: 16px 20px; background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(239, 68, 68, 0.15) 100%); border: 1px solid rgba(239, 68, 68, 0.3); border-radius: var(--border-radius); color: #b91c1c; font-weight: 600; margin-bottom: 20px; }

    /* No data / empty states */
    .no-data, .empty-state { text-align: center; padding: 60px 20px; color: var(--text-secondary); }
    .no-data-icon, .empty-state-icon { font-size: 64px; margin-bottom: 16px; opacity: 0.5; }
    .empty-state-text { font-size: 18px; font-weight: 600; }
    .no-result { color: var(--text-secondary); font-size: 16px; text-align: center; padding: 40px; }

    /* Action bar */
    .action-bar { display: flex; gap: 12px; margin-top: 24px; padding-top: 20px; border-top: 1px solid rgba(0,0,0,0.06); flex-wrap: wrap; }

    /* Footer */
    .footer { margin-top: 32px; padding-top: 20px; border-top: 1px solid rgba(0,0,0,0.06); }
    .back-link { display: inline-flex; align-items: center; gap: 8px; color: var(--primary-color); text-decoration: none; font-weight: 600; transition: color 0.2s; }
    .back-link:hover { color: var(--primary-dark); }

    /* Dates grid */
    .dates-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; margin-top: 24px; }
    .date-card { display: flex; align-items: center; gap: 16px; padding: 20px 24px; background: linear-gradient(135deg, rgba(99, 102, 241, 0.05) 0%, rgba(79, 70, 229, 0.08) 100%); border: 1px solid rgba(99, 102, 241, 0.15); border-radius: var(--border-radius-lg); text-decoration: none; color: var(--text-primary); transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
    .date-card::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent); transition: left 0.5s; }
    .date-card:hover::before { left: 100%; }
    .date-card:hover { transform: translateY(-4px) scale(1.02); box-shadow: var(--shadow-xl); border-color: var(--primary-color); background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(79, 70, 229, 0.15) 100%); }
    .date-icon { width: 56px; height: 56px; background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); border-radius: var(--border-radius); display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: var(--shadow-md); }
    .date-info { flex: 1; }
    .date-value { font-size: 18px; font-weight: 700; color: var(--text-primary); }
    .date-label { font-size: 13px; color: var(--text-secondary); font-weight: 500; }
    .date-arrow { width: 32px; height: 32px; background: rgba(99, 102, 241, 0.1); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--primary-color); font-size: 14px; transition: all 0.3s ease; }
    .date-card:hover .date-arrow { background: var(--primary-color); color: white; transform: translateX(4px); }

    /* Hours / groups grid */
    .hours-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; }
    .hour-card { display: flex; align-items: center; gap: 16px; padding: 20px; background: linear-gradient(135deg, rgba(16, 185, 129, 0.05) 0%, rgba(16, 185, 129, 0.1) 100%); border: 1px solid rgba(16, 185, 129, 0.2); border-radius: var(--border-radius-lg); text-decoration: none; color: var(--text-primary); transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative; overflow: hidden; }
    .hour-card::before { content: ''; position: absolute; top: 0; left: -100%; width: 100%; height: 100%; background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent); transition: left 0.5s; }
    .hour-card:hover::before { left: 100%; }
    .hour-card:hover { transform: translateY(-4px) scale(1.02); box-shadow: var(--shadow-xl); border-color: var(--accent-color); background: linear-gradient(135deg, rgba(16, 185, 129, 0.1) 0%, rgba(16, 185, 129, 0.18) 100%); }
    .hour-icon { width: 48px; height: 48px; background: linear-gradient(135deg, var(--accent-color) 0%, #059669 100%); border-radius: var(--border-radius); display: flex; align-items: center; justify-content: center; font-size: 20px; box-shadow: var(--shadow-md); }
    .hour-value { font-size: 20px; font-weight: 700; color: var(--text-primary); }
    .hour-arrow { margin-left: auto; width: 28px; height: 28px; background: rgba(16, 185, 129, 0.1); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--accent-color); font-size: 12px; transition: all 0.3s ease; }
    .hour-card:hover .hour-arrow { background: var(--accent-color); color: white; transform: translateX(4px); }

    /* Buttons */
    .back-btn { display: inline-flex; align-items: center; gap: 8px; margin-top: 24px; padding: 12px 24px; background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); color: white; text-decoration: none; border-radius: var(--border-radius); font-weight: 600; transition: all 0.3s ease; box-shadow: var(--shadow-md); }
    .back-btn:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }

    .btn { display: inline-flex; align-items: center; gap: 8px; padding: 10px 18px; border-radius: var(--border-radius); font-weight: 700; font-size: 14px; text-decoration: none; border: none; cursor: pointer; transition: all 0.2s ease; }
    .btn-primary { background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%); color: white; box-shadow: var(--shadow-md); }
    .btn-primary:hover { transform: translateY(-2px); box-shadow: var(--shadow-lg); }
    .btn-secondary { background: rgba(66, 153, 225, 0.1); color: var(--primary-dark); }
    .btn-secondary:hover { background: rgba(66, 153, 225, 0.2); }

    @media (max-width: 900px) {
        .app-layout { flex-direction: column; padding: 16px; }
        .hours-grid { grid-template-columns: 1fr; }
        .dates-grid { grid-template-columns: 1fr; }
        .dashboard { grid-template-columns: repeat(2, 1fr); }
        .info-grid { grid-template-columns: 1fr; }
        .vehicle-grid { grid-template-columns: 1fr; }
        .page-title { font-size: 26px; }
        .vehicule-header { flex-direction: column; align-items: flex-start; }
    }
</style>