import {
  DropdownMenu, DropdownMenuTrigger, DropdownMenuContent,
  DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";

interface GenderSelectProps {
  value: "MALE" | "FEMALE" | "UNDEFINED";
  onSelect: (val: "MALE" | "FEMALE" | "UNDEFINED") => void;
}

const GENDER_LABELS: Record<GenderSelectProps["value"], string> = {
  MALE: "Male",
  FEMALE: "Female",
  UNDEFINED: "Prefer not to say",
};

const GENDER_OPTIONS: GenderSelectProps["value"][] = ["MALE", "FEMALE", "UNDEFINED"];

export const GenderSelect = ({ value, onSelect }: GenderSelectProps) => (
  <div className="space-y-1">
    <label className="block font-display text-xs uppercase tracking-wide text-[#4A4136]">Gender</label>
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="outline"
          className="border-[#C9A063] bg-[#F3EBD9] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm w-full justify-start"
        >
          {GENDER_LABELS[value]}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56 bg-[#F3EBD9] border-[#C9A063] rounded-sm">
        <DropdownMenuLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">
          Choose your gender
        </DropdownMenuLabel>
        <DropdownMenuSeparator className="bg-[#C9A063]" />
        {GENDER_OPTIONS.map((option) => (
          <DropdownMenuItem
            key={option}
            onSelect={() => onSelect(option)}
            className="text-[#241F1A] focus:bg-[#B23A2E] focus:text-[#F3EBD9] rounded-sm"
          >
            {GENDER_LABELS[option]}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  </div>
);