import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { Button } from './ui'
import { Icon, type IconName } from './Icon'
import { useAuth } from '../auth/AuthContext'

const nav: { to: string; label: string; icon: IconName }[] = [
  { to: '/dashboard', label: 'Overview', icon: 'Home' }, { to: '/explore', label: 'Explore', icon: 'Compass' },
  { to: '/planner', label: 'Study planner', icon: 'CalendarDays' }, { to: '/library', label: 'Study library', icon: 'Library' },
  { to: '/analytics', label: 'Analytics', icon: 'TrendingUp' },
]

export function Layout() {
  const { user } = useAuth()
  const [open, setOpen] = useState(false)
  const location = useLocation()
  const title = nav.find((item) => location.pathname.startsWith(item.to))?.label ?? 'LearnWise'
  return <div className="app-shell">
    <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
      <div className="brand"><span className="brand-mark"><Icon name="Sparkles" size={17} /></span><span>learnwise</span></div>
      <nav aria-label="Primary navigation">{nav.map((item) => <NavLink key={item.to} to={item.to} onClick={() => setOpen(false)} className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}><Icon name={item.icon} size={18} /><span>{item.label}</span></NavLink>)}</nav>
      <div className="sidebar-bottom"><NavLink to="/tutor" className="tutor-prompt"><span className="tutor-icon"><Icon name="Brain" size={17} /></span><span><strong>Ask your tutor</strong><small>Get unstuck, gently</small></span><Icon name="ArrowRight" size={15} /></NavLink><NavLink to="/profile" className="nav-item"><Icon name="Settings" size={18} /><span>Preferences</span></NavLink></div>
    </aside>
    {open && <button className="sidebar-overlay" aria-label="Close menu" onClick={() => setOpen(false)} />}
    <main className="main-content"><header className="topbar"><Button variant="ghost" icon="Menu" aria-label="Open menu" className="menu-button" onClick={() => setOpen(true)} /><div className="breadcrumb"><span>Workspace</span><Icon name="ChevronRight" size={14} /><strong>{title}</strong></div><div className="topbar-actions"><button className="icon-button" aria-label="Help"><Icon name="CircleHelp" /></button><button className="icon-button notification" aria-label="Notifications"><Icon name="Bell" /><i /></button><NavLink to="/profile" className="avatar" aria-label="Open profile">{user?.displayName.split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part[0].toUpperCase()).join('')}</NavLink></div></header><div className="page-content"><Outlet /></div></main>
  </div>
}
