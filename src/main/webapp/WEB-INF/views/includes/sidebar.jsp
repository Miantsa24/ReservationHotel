<style>
/* ===== App Layout ===== */
.app-layout {
    display: flex;
    gap: 28px;
    max-width: 1400px;
    margin: 0 auto;
    padding: 24px;
    min-height: 100vh;
    align-items: flex-start;
}
.main-content { flex: 1; min-width: 0; }

/* ===== Sidebar ===== */
.sidebar {
    width: 270px;
    flex-shrink: 0;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(20px);
    border-radius: var(--border-radius-large, 16px);
    box-shadow: var(--shadow-medium, 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05));
    border: 1px solid rgba(255, 255, 255, 0.2);
    padding: 28px 20px;
    position: sticky;
    top: 24px;
    animation: sidebarFadeIn 0.5s ease-out;
}
@keyframes sidebarFadeIn {
    from { opacity: 0; transform: translateX(-20px); }
    to   { opacity: 1; transform: translateX(0); }
}
.sidebar-brand {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 28px;
    padding-bottom: 20px;
    border-bottom: 2px solid var(--secondary-color, #e2e8f0);
}
.sidebar-brand .brand-icon {
    font-size: 28px;
}
.sidebar-brand .brand-text {
    font-size: 20px;
    font-weight: 800;
    letter-spacing: -0.025em;
    background: linear-gradient(135deg, var(--primary-color, #4299e1) 0%, var(--primary-dark, #3182ce) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}
.sidebar-nav {
    list-style: none;
    padding: 0;
    margin: 0;
}
.sidebar-nav li {
    margin: 4px 0;
}
.sidebar-nav a {
    display: flex;
    align-items: center;
    gap: 12px;
    text-decoration: none;
    color: var(--text-secondary, #4a5568);
    padding: 12px 16px;
    border-radius: var(--border-radius, 12px);
    font-weight: 600;
    font-size: 15px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;
}
.sidebar-nav a::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(66, 153, 225, 0.06), transparent);
    transition: left 0.4s;
}
.sidebar-nav a:hover::before {
    left: 100%;
}
.sidebar-nav a:hover {
    color: var(--primary-dark, #3182ce);
    background: rgba(66, 153, 225, 0.08);
    transform: translateX(4px);
}
.sidebar-nav a .nav-icon {
    font-size: 20px;
    flex-shrink: 0;
    width: 28px;
    text-align: center;
}
.sidebar-nav a.active {
    background: linear-gradient(135deg, var(--primary-color, #4299e1) 0%, var(--primary-dark, #3182ce) 100%);
    color: white;
    -webkit-text-fill-color: white;
    box-shadow: 0 4px 12px rgba(66, 153, 225, 0.35);
}
.sidebar-nav a.active:hover {
    transform: translateX(0);
    box-shadow: 0 6px 16px rgba(66, 153, 225, 0.45);
}

/* Responsive */
@media (max-width: 900px) {
    .app-layout { flex-direction: column; padding: 12px; }
    .sidebar { width: 100%; position: static; }
    .sidebar-nav { display: flex; flex-wrap: wrap; gap: 8px; }
    .sidebar-nav li { margin: 0; }
}
</style>

<div class="sidebar">
    <div class="sidebar-brand">
        <span class="brand-text">Hotel App</span>
    </div>
    <ul class="sidebar-nav">
        <li><a href="<%= request.getContextPath() %>/index" class="<%= request.getRequestURI().contains("index") ? "active" : "" %>"> Accueil</a></li>
        <li><a href="<%= request.getContextPath() %>/vehicules" class="<%= request.getRequestURI().contains("vehicule") ? "active" : "" %>"> Vehicules</a></li>
        <li><a href="<%= request.getContextPath() %>/reservations" class="<%= request.getRequestURI().contains("reservation") ? "active" : "" %>"> Reservations</a></li>
        <li><a href="<%= request.getContextPath() %>/tracabilite" class="<%= request.getRequestURI().contains("tracabilite") ? "active" : "" %>"> Tracabilite</a></li>
    </ul>
</div>
