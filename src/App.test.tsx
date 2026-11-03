import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import { AuthProvider } from './auth/AuthContext'

beforeEach(() => {
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue(
    new Response(JSON.stringify({ id: 1, email: 'learner@example.com', displayName: 'Learner' }), { status: 200 }),
  ))
})

describe('LearnWise routes', () => {
  it('renders the landing page', () => {
    render(<MemoryRouter initialEntries={['/']}><App /></MemoryRouter>)
    expect(screen.getByText('Make progress')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /sign in/i })).toBeInTheDocument()
  })

  it('renders the dashboard with fixture notice', async () => {
    render(<MemoryRouter initialEntries={['/dashboard']}><AuthProvider><App /></AuthProvider></MemoryRouter>)
    await waitFor(() => expect(screen.getByText(/Good (morning|afternoon|evening), Learner\./)).toBeInTheDocument())
    expect(screen.getByText(/development fixture/i)).toBeInTheDocument()
  })

  it('renders the tutor and supports suggestion prompts', async () => {
    render(<MemoryRouter initialEntries={['/tutor']}><AuthProvider><App /></AuthProvider></MemoryRouter>)
    await waitFor(() => expect(screen.getByText('Meet your AI tutor.')).toBeInTheDocument())
    expect(screen.getByRole('button', { name: /explain a concept/i })).toBeInTheDocument()
  })
})
