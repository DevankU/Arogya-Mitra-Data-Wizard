'use client'

import React from "react"

import { createClient } from '@/lib/supabase/client'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'

// Doctor email - this user is always assigned as Doctor
const DOCTOR_EMAIL = 'aravsaxena884@gmail.com'

export default function SignUpPage() {
  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [repeatPassword, setRepeatPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()

  const isDoctor = email.toLowerCase() === DOCTOR_EMAIL.toLowerCase()
  const role = isDoctor ? 'doctor' : 'patient'

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault()
    const supabase = createClient()
    setIsLoading(true)
    setError(null)

    if (password !== repeatPassword) {
      setError('Passwords do not match')
      setIsLoading(false)
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      setIsLoading(false)
      return
    }

    try {
      const { error } = await supabase.auth.signUp({
        email,
        password,
        options: {
          emailRedirectTo:
            process.env.NEXT_PUBLIC_DEV_SUPABASE_REDIRECT_URL ||
            `${window.location.origin}/chat`,
          data: {
            full_name: fullName,
            role: role,
            avatar_url: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(fullName)}&backgroundColor=${isDoctor ? '10b981' : '3b82f6'}`,
          },
        },
      })
      if (error) throw error
      router.push('/auth/sign-up-success')
    } catch (error: unknown) {
      setError(error instanceof Error ? error.message : 'An error occurred')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#FFFAF8] via-[#FFF5F2] to-[#FFEBE5] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="flex flex-col gap-6">
          <div className="text-center mb-4">
            <div className="w-16 h-16 bg-[#FFAB91]/20 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg xmlns="http://www.w3.org/2000/svg" className="w-8 h-8 text-[#FFAB91]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
            </div>
            <h1 className="text-3xl font-bold text-[#2D3436] tracking-tight">MedConsult AI</h1>
            <p className="text-[#636E72] font-medium mt-1">Create Your Account</p>
          </div>
          
          <Card className="border-0 shadow-xl">
            <CardHeader className="space-y-1">
              <CardTitle className="text-2xl text-[#2D3436]">Sign Up</CardTitle>
              <CardDescription className="text-[#636E72]">
                Join MedConsult AI for secure medical consultations
              </CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleSignUp}>
                <div className="flex flex-col gap-4">
                  <div className="grid gap-2">
                    <Label htmlFor="fullName" className="text-[#2D3436] font-semibold">Full Name</Label>
                    <Input
                      id="fullName"
                      type="text"
                      placeholder="Dr. John Smith or John Smith"
                      required
                      value={fullName}
                      onChange={(e) => setFullName(e.target.value)}
                      className="h-12 rounded-xl border-stone-200 focus:border-[#FFAB91] focus:ring-[#FFAB91]/20"
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="email" className="text-[#2D3436] font-semibold">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="your@email.com"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="h-12 rounded-xl border-stone-200 focus:border-[#FFAB91] focus:ring-[#FFAB91]/20"
                    />
                    {email && (
                      <div className={`flex items-center gap-2 px-3 py-2 rounded-lg ${isDoctor ? 'bg-emerald-50 border border-emerald-200' : 'bg-blue-50 border border-blue-200'}`}>
                        <div className={`w-2 h-2 rounded-full ${isDoctor ? 'bg-emerald-500' : 'bg-blue-500'}`}></div>
                        <p className={`text-xs font-semibold ${isDoctor ? 'text-emerald-700' : 'text-blue-700'}`}>
                          You will be registered as: <span className="uppercase">{role}</span>
                        </p>
                      </div>
                    )}
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="password" className="text-[#2D3436] font-semibold">Password</Label>
                    <Input
                      id="password"
                      type="password"
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="h-12 rounded-xl border-stone-200 focus:border-[#FFAB91] focus:ring-[#FFAB91]/20"
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="repeat-password" className="text-[#2D3436] font-semibold">Confirm Password</Label>
                    <Input
                      id="repeat-password"
                      type="password"
                      required
                      value={repeatPassword}
                      onChange={(e) => setRepeatPassword(e.target.value)}
                      className="h-12 rounded-xl border-stone-200 focus:border-[#FFAB91] focus:ring-[#FFAB91]/20"
                    />
                  </div>
                  {error && (
                    <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm">
                      {error}
                    </div>
                  )}
                  <Button 
                    type="submit" 
                    className="w-full h-12 bg-[#FFAB91] hover:bg-[#FF9A7B] text-white rounded-xl font-bold text-base shadow-lg shadow-[#FFAB91]/30 mt-2" 
                    disabled={isLoading}
                  >
                    {isLoading ? 'Creating account...' : 'Create Account'}
                  </Button>
                </div>
                <div className="mt-6 text-center text-sm text-[#636E72]">
                  Already have an account?{' '}
                  <Link
                    href="/auth/login"
                    className="text-[#FFAB91] font-semibold hover:underline"
                  >
                    Sign in
                  </Link>
                </div>
              </form>
            </CardContent>
          </Card>
          
          <div className="flex items-center justify-center gap-2 text-xs text-[#636E72]/70">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
            </svg>
            <span>Your data is encrypted and secure</span>
          </div>
        </div>
      </div>
    </div>
  )
}
