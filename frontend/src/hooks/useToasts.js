import { useCallback, useEffect, useRef, useState } from 'react'

const DURATION = 4000

export function useToasts() {
  const [toasts, setToasts] = useState([])
  const nextId = useRef(1)
  const timers = useRef(new Map())

  const dismissToast = useCallback((id) => {
    clearTimeout(timers.current.get(id))
    timers.current.delete(id)
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (message, type = 'success') => {
      const id = nextId.current++
      setToasts((current) => [...current, { id, message, type }])
      timers.current.set(
        id,
        setTimeout(() => dismissToast(id), DURATION),
      )
    },
    [dismissToast],
  )

  useEffect(() => {
    const pending = timers.current
    return () => pending.forEach((timer) => clearTimeout(timer))
  }, [])

  return { toasts, showToast, dismissToast }
}
