import { FirstSteps } from './InfoSection/FirstSteps'
import { NewsList } from './InfoSection/NewsList'

export function InfoSection() {
  return (
    <div className="w-full min-h-screen flex flex-col bg-card/40 border-l border-border/40 backdrop-blur-sm">
      <div className="flex-1 flex items-center justify-center p-8 border-b border-border/40">
        <FirstSteps />
      </div>
      <div className="flex-1 flex items-center justify-center p-8">
        <NewsList />
      </div>
    </div>
  );
}