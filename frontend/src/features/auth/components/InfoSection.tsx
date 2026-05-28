import { FirstSteps } from './InfoSection/FirstSteps'
import { NewsList } from './InfoSection/NewsList'

export function InfoSection() {
  return (
    <div className="h-screen flex flex-col bg-card border-l border-border/50 overflow-hidden">
      <FirstSteps />
      <NewsList />
    </div>
  )
}