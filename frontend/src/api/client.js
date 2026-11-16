export const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(message, status = 0, fields = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fields = fields
  }
}

export async function request(path, { method = 'GET', body, signal } = {}) {
  let response

  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      signal,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError(
      'Não foi possível conectar à API. Verifique se o backend está rodando em ' + API_URL + '.',
    )
  }

  // 204 No Content (ex.: DELETE) não tem corpo.
  if (response.status === 204) return null

  const data = await response.json().catch(() => null)

  if (!response.ok) {
    throw new ApiError(
      data?.message ?? `A API respondeu com erro ${response.status}.`,
      response.status,
      data?.fields ?? {},
    )
  }

  return data
}
