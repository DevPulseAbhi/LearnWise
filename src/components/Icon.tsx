import { ArrowRight, Bell, BookOpen, Brain, CalendarDays, Check, ChevronDown, ChevronLeft, ChevronRight, CircleHelp, Clock3, Compass, FileText, Home, Library, Menu, MoreHorizontal, Play, Plus, Search, Settings, Sparkles, Target, Trash2, TrendingUp, UserRound, X } from 'lucide-react'

const icons = { ArrowRight, Bell, BookOpen, Brain, CalendarDays, Check, ChevronDown, ChevronLeft, ChevronRight, CircleHelp, Clock3, Compass, FileText, Home, Library, Menu, MoreHorizontal, Play, Plus, Search, Settings, Sparkles, Target, Trash2, TrendingUp, UserRound, X }
export type IconName = keyof typeof icons

export function Icon({ name, size = 18, strokeWidth = 1.8 }: { name: IconName; size?: number; strokeWidth?: number }) {
  const Component = icons[name]
  return <Component aria-hidden="true" size={size} strokeWidth={strokeWidth} />
}
