import { FirstSteps } from './InfoSection/FirstSteps';
import { NewsList } from './InfoSection/NewsList';

export function InfoSection() {
  return (
    <div className="flex min-h-screen w-full flex-col border-l border-border/40 bg-card/40 backdrop-blur-sm">
      <div className="flex flex-1 items-center justify-center border-b border-border/40 p-8">
        <NewsList />
      </div>

      <div className="flex flex-1 items-center justify-center p-8">
        <FirstSteps />
      </div>
    </div>
  );
}