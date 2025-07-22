import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent,
  DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

interface GenderSelectProps {
  value: "MALE" | "FEMALE" | "UNDEFINED";
  onSelect: (val: "MALE" | "FEMALE" | "UNDEFINED") => void;
}

export const GenderSelect = ({ value, onSelect }: GenderSelectProps) => (
  <div className="space-y-1">
    <label className="block text-sm font-medium">Gender</label>
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{value}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuLabel>Choose your gender</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {["MALE", "FEMALE", "UNDEFINED"].map((option) => (
          <DropdownMenuItem
            key={option}
            onSelect={() => onSelect(option as GenderSelectProps["value"])}
          >
            {option}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  </div>
);
