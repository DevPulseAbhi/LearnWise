import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'
import { Icon, type IconName } from './Icon'

export function Button({ children, variant = 'primary', icon, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'ghost' | 'soft'; icon?: IconName }) {
  return <button type="button" className={`button button-${variant}`} {...props}>{icon && <Icon name={icon} size={16} />}{children}</button>
}

export function Card({ children, className = '', inset = false, id }: { children: ReactNode; className?: string; inset?: boolean; id?: string }) {
  return <section id={id} className={`card ${inset ? 'card-inset' : ''} ${className}`}>{children}</section>
}

export function Badge({ children, tone = 'neutral' }: { children: ReactNode; tone?: 'neutral' | 'sage' | 'blue' | 'peach' | 'lavender' }) {
  return <span className={`badge badge-${tone}`}>{children}</span>
}

export function Progress({ value, label, tone = 'sage' }: { value: number; label?: string; tone?: string }) {
  return <div className="progress-wrap">{label && <div className="progress-label"><span>{label}</span><strong>{value}%</strong></div>}<div className="progress-track"><div className={`progress-value progress-${tone}`} style={{ width: `${value}%` }} /></div></div>
}

export function TextInput({ label, icon, ...props }: InputHTMLAttributes<HTMLInputElement> & { label?: string; icon?: IconName }) {
  return <label className="field">{label && <span className="field-label">{label}</span>}<span className="input-wrap">{icon && <Icon name={icon} size={17} />}<input {...props} /></span></label>
}

export function Select({ label, children, ...props }: SelectHTMLAttributes<HTMLSelectElement> & { label?: string }) {
  return <label className="field">{label && <span className="field-label">{label}</span>}<span className="select-wrap"><select {...props}>{children}</select><Icon name="ChevronDown" size={16} /></span></label>
}

export function EmptyState({ icon = 'Library', title, description, action }: { icon?: IconName; title: string; description: string; action?: ReactNode }) {
  return <div className="empty-state"><div className="empty-icon"><Icon name={icon} size={24} /></div><h3>{title}</h3><p>{description}</p>{action}</div>
}

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: ReactNode }) {
  return <div className="page-header"><div>{eyebrow && <p className="eyebrow">{eyebrow}</p>}<h1>{title}</h1>{description && <p className="page-description">{description}</p>}</div>{action}</div>
}
