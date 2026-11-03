import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'

export type AuthState = 'loading' | 'signed_out' | 'signing_in' | 'signed_in' | 'session_expired' | 'authentication_error'
export type UserProfile = { id: number; email: string; displayName: string; avatarUrl?: string | null }
const apiBase = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/learnwise/api/v1').replace(/\/+$/, '')

type AuthContextValue = {
  state: AuthState
  user: UserProfile | null
  signIn: (email: string, password: string) => Promise<string | null>
  register: (name: string, email: string, password: string, confirmPassword: string) => Promise<string | null>
  signOut: () => Promise<void>
  refresh: () => Promise<void>
}
const AuthContext = createContext<AuthContextValue | null>(null)

async function readError(response: Response) {
  try { return (await response.json() as { message?: string }).message ?? 'Authentication failed' } catch { return 'Authentication failed' }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>('loading')
  const [user, setUser] = useState<UserProfile | null>(null)
  const refresh = async () => {
    try {
      const response = await fetch(`${apiBase}/auth/me`, { credentials: 'include' })
      if (response.ok) { setUser(await response.json() as UserProfile); setState('signed_in') }
      else if (response.status === 401) { setUser(null); setState((current) => current === 'loading' ? 'signed_out' : 'session_expired') }
      else { setUser(null); setState('authentication_error') }
    } catch {
      // An anonymous first visit must remain usable when the API is temporarily unavailable.
      // A failure after sign-in is retained as an explicit session verification error.
      setUser(null)
      setState((current) => current === 'loading' ? 'signed_out' : 'authentication_error')
    }
  }
  useEffect(() => { void refresh() }, [])
  const authenticate = async (path: 'login' | 'register', body: object) => {
    setState('signing_in')
    try {
      const response = await fetch(`${apiBase}/auth/${path}`, { method: 'POST', credentials: 'include', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
      if (!response.ok) { setState('signed_out'); return readError(response) }
      setUser(await response.json() as UserProfile); setState('signed_in'); return null
    } catch { setState('authentication_error'); return 'Unable to reach LearnWise. Please try again.' }
  }
  const value = useMemo<AuthContextValue>(() => ({
    state, user, refresh,
    signIn: (email, password) => authenticate('login', { email, password }),
    register: (name, email, password, confirmPassword) => authenticate('register', { name, email, password, confirmPassword }),
    signOut: async () => {
      const csrf = document.cookie.split('; ').find((value) => value.startsWith('LW_CSRF='))?.split('=')[1]
      await fetch(`${apiBase}/auth/logout`, { method: 'POST', credentials: 'include', headers: csrf ? { 'X-CSRF-Token': decodeURIComponent(csrf) } : {} })
      setUser(null); setState('signed_out')
    },
  }), [state, user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}

export function AuthStateMessage({ state }: { state: AuthState }) {
  const messages: Record<AuthState, string> = {
    loading: 'Checking your session…', signed_out: 'You are signed out.', signing_in: 'Signing you in…',
    signed_in: 'You are signed in.', session_expired: 'Your session expired. Please sign in again.',
    authentication_error: 'We could not verify your session. Please try again.',
  }
  return <p className={`auth-state auth-state-${state}`} role="status">{messages[state]}</p>
}
